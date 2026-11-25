package edu.harvard.iq.dataverse.uard;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ReadListener;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstitutionRegistrationServletTest {
    InstitutionRegistrationServlet servlet;
    HttpServletRequest request;
    HttpServletResponse response;
    InstitutionRegistrationPage.Challenge challenge;
    StringWriter body;
    java.util.Map<String, String> fields;

    @BeforeEach void setup() throws Exception {
        servlet = new InstitutionRegistrationServlet();
        servlet.applications = mock(InstitutionApplicationService.class);
        servlet.notifier = mock(InstitutionApplicationNotifier.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        body = new StringWriter(); when(response.getWriter()).thenReturn(new PrintWriter(body));
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        challenge = new InstitutionRegistrationPage.Challenge();
        var challenges = new LinkedHashMap<String, InstitutionRegistrationPage.Challenge>();
        challenges.put(challenge.token, challenge);
        when(session.getAttribute(InstitutionRegistrationPage.class.getName())).thenReturn(challenges);
        when(request.getHeader("X-Uard-Token")).thenReturn(challenge.token);
        fields = InstitutionApplicationValidatorTest.fields();
        fields.put("captcha", Integer.toString(challenge.left + challenge.right));
        fields.put("consent", "on");
        when(request.getContentType()).thenReturn("multipart/form-data; boundary=uard-test");
        when(request.getInputStream()).thenAnswer(call -> {
            byte[] bytes = multipart(fields, InstitutionApplicationValidatorTest.image("png"));
            var input = new ByteArrayInputStream(bytes);
            return new ServletInputStream() {
                @Override public int read() { return input.read(); }
                @Override public boolean isFinished() { return input.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) { }
            };
        });
    }

    @Test void rejectsMissingOrForeignSessionTokenBeforeReadingMultipart() throws Exception {
        when(request.getHeader("X-Uard-Token")).thenReturn(UUID.randomUUID().toString());
        servlet.doPost(request, response);
        verify(response).setStatus(403);
        verify(request, never()).getInputStream();
        verifyNoInteractions(servlet.applications, servlet.notifier);
    }
    @Test void rejectsWrongChallengeAndInvalidFieldsWithoutPersistence() throws Exception {
        fields.put("captcha", "99");
        servlet.doPost(request, response);
        verify(response).setStatus(422);
        assertEquals(1, challenge.failures);
        verifyNoInteractions(servlet.applications);
        fields.put("captcha", Integer.toString(challenge.left + challenge.right));
        fields.put("email", "bad");
        servlet.doPost(request, response);
        assertTrue(body.toString().contains("\"field\":\"email\""));
        verifyNoInteractions(servlet.applications);
    }
    @Test void savesOnceAndAcknowledgesReplayWithoutAnotherEmail() throws Exception {
        servlet.doPost(request, response);
        servlet.doPost(request, response);
        verify(servlet.applications, times(1)).save(eq(UUID.fromString(challenge.token)), any());
        verify(servlet.notifier, times(1)).wake();
        assertTrue(challenge.completed);
        assertTrue(body.toString().contains(challenge.token));
    }
    @Test void databaseFailureCannotBecomeSuccessAndCanBeRetried() throws Exception {
        doThrow(new SQLException("unavailable", "08001")).doNothing().when(servlet.applications).save(any(), any());
        servlet.doPost(request, response);
        verify(response).setStatus(503);
        assertFalse(challenge.completed);
        verifyNoInteractions(servlet.notifier);
        servlet.doPost(request, response);
        assertTrue(challenge.completed);
        verify(servlet.notifier).wake();
    }
    @Test void notificationSchedulingFailureDoesNotLoseSavedApplication() throws Exception {
        doThrow(new IllegalStateException()).when(servlet.notifier).wake();
        servlet.doPost(request, response);
        assertTrue(challenge.completed);
        assertTrue(body.toString().contains("\"ok\":true"));
    }
    @Test void oversizeMultipartIsRejected() throws Exception {
        when(request.getContentLengthLong()).thenReturn(4L * 1024 * 1024);
        servlet.doPost(request, response);
        verify(response).setStatus(413);
        verifyNoInteractions(servlet.applications);
    }
    @Test void oversizeRequestIsRejectedBeforeMultipartParsingOrCdiAccess() throws Exception {
        when(request.getContentLengthLong()).thenReturn(3L * 1024 * 1024 + 1);
        servlet.doPost(request, response);
        verify(response).setStatus(413);
        verify(request, never()).getParameter(anyString());
        verifyNoInteractions(servlet.applications);
    }
    static byte[] multipart(java.util.Map<String, String> fields, byte[] image) throws Exception {
        var output = new java.io.ByteArrayOutputStream();
        for (var entry : fields.entrySet()) {
            output.write(("--uard-test\r\nContent-Disposition: form-data; name=\"" + entry.getKey()
                    + "\"\r\n\r\n" + entry.getValue() + "\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        output.write("--uard-test\r\nContent-Disposition: form-data; name=\"logo\"; filename=\"logo.png\"\r\nContent-Type: image/png\r\n\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        output.write(image);
        output.write("\r\n--uard-test--\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return output.toByteArray();
    }
}