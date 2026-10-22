package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataverseRequestServiceBeanTest {
    @Mock DataverseSession session;
    @Mock HttpServletRequest httpRequest;
    @InjectMocks DataverseRequestServiceBean service;

    @ParameterizedTest
    @ValueSource(strings = {"uk", "ua", "en"})
    void capturesNativeSessionLocaleBeforeBackgroundWork(String language) {
        when(session.getUser()).thenReturn(new AuthenticatedUser());
        String[] selectedLanguage = {language};
        when(session.getLocaleCode()).thenAnswer(call -> selectedLanguage[0]);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        service.setup();
        DataverseRequest request = service.getDataverseRequest();
        assertEquals(Locale.forLanguageTag(language), request.getNotificationLocale());
        // A later language change must not change the already queued operation.
        selectedLanguage[0] = "de";
        assertEquals(Locale.forLanguageTag(language), request.getNotificationLocale());
    }

    @Test
    void apiRequestDoesNotAcquireAnImplicitNotificationLocale() {
        DataverseRequest request = new DataverseRequest(new AuthenticatedUser(), (HttpServletRequest) null);
        assertNull(request.getNotificationLocale());
    }
}
