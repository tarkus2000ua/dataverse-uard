package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.branding.BrandingUtil;
import edu.harvard.iq.dataverse.feedback.ContactCaptchaService;
import edu.harvard.iq.dataverse.feedback.Feedback;
import edu.harvard.iq.dataverse.feedback.FeedbackUtil;
import edu.harvard.iq.dataverse.feedback.TurnstileVerifier;
import edu.harvard.iq.dataverse.util.BundleUtil;
import edu.harvard.iq.dataverse.util.JsfHelper;
import edu.harvard.iq.dataverse.util.SystemConfig;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SendFeedbackDialogCaptchaTest {
    private SendFeedbackDialog dialog() {
        SendFeedbackDialog dialog = spy(new SendFeedbackDialog());
        dialog.contactCaptcha = mock(ContactCaptchaService.class);
        dialog.mailService = mock(MailServiceBean.class);
        dialog.systemConfig = mock(SystemConfig.class);
        dialog.dataverseSession = mock(DataverseSession.class);
        when(dialog.systemConfig.getDataverseSiteUrl()).thenReturn("http://localhost:8080");
        doReturn(false).when(dialog).ccSupport();
        return dialog;
    }

    @ParameterizedTest
    @EnumSource(value = TurnstileVerifier.Result.class, names = {"REJECTED", "UNAVAILABLE"})
    void rejectedOrUnavailableVerificationNeverSendsMail(TurnstileVerifier.Result result) {
        SendFeedbackDialog dialog = dialog();
        FacesContext faces = mock(FacesContext.class);
        ExternalContext external = mock(ExternalContext.class);
        when(faces.getExternalContext()).thenReturn(external);
        when(external.getRequestServerName()).thenReturn("localhost");
        when(dialog.contactCaptcha.verify("token", "localhost")).thenReturn(result);
        dialog.setTurnstileToken("token");
        try (var context = mockStatic(FacesContext.class); var bundles = mockStatic(BundleUtil.class)) {
            context.when(FacesContext::getCurrentInstance).thenReturn(faces);
            bundles.when(() -> BundleUtil.getStringFromBundle(anyString())).thenReturn("Try verification again");
            dialog.sendMessage();
            verify(faces).validationFailed();
            verify(faces).addMessage(eq("emailForm:turnstileToken"), any());
            verifyNoInteractions(dialog.mailService);
            assertEquals("", dialog.getTurnstileToken());
        }
    }

    @Test
    void verifiedSubmissionUsesExistingMailPathWithMockedDelivery() {
        SendFeedbackDialog dialog = dialog();
        FacesContext faces = mock(FacesContext.class);
        ExternalContext external = mock(ExternalContext.class);
        when(faces.getExternalContext()).thenReturn(external);
        when(external.getRequestServerName()).thenReturn("localhost");
        when(dialog.contactCaptcha.verify("token", "localhost")).thenReturn(TurnstileVerifier.Result.VERIFIED);
        dialog.setTurnstileToken("token");
        Feedback feedback = new Feedback("sender@example.org", "support@example.org", null, "Subject", "Message");
        try (var context = mockStatic(FacesContext.class);
                var branding = mockStatic(BrandingUtil.class);
                var feedbacks = mockStatic(FeedbackUtil.class);
                var bundles = mockStatic(BundleUtil.class);
                var jsf = mockStatic(JsfHelper.class)) {
            context.when(FacesContext::getCurrentInstance).thenReturn(faces);
            branding.when(BrandingUtil::getInstallationBrandName).thenReturn("UaRD");
            branding.when(() -> BrandingUtil.getSupportTeamName(null)).thenReturn("Support");
            feedbacks.when(() -> FeedbackUtil.gatherFeedback(null, dialog.dataverseSession, "", "", null, "",
                    "http://localhost:8080", "UaRD", "Support", false)).thenReturn(feedback);
            dialog.sendMessage();
            verify(dialog.mailService).sendMail("sender@example.org", "support@example.org", null, "Subject", "Message");
            verify(faces, never()).validationFailed();
            assertEquals("", dialog.getTurnstileToken());
        }
    }

    @Test
    void directActionCannotBypassArithmeticFallback() {
        SendFeedbackDialog dialog = dialog();
        when(dialog.contactCaptcha.isMathEnabled()).thenReturn(true);
        FacesContext faces = mock(FacesContext.class);
        try (var context = mockStatic(FacesContext.class); var bundles = mockStatic(BundleUtil.class)) {
            context.when(FacesContext::getCurrentInstance).thenReturn(faces);
            dialog.op1 = 1L;
            dialog.op2 = 2L;
            for (Long answer : new Long[] {null, 4L}) {
                dialog.setUserSum(answer);
                dialog.sendMessage();
            }
            verify(faces, times(2)).validationFailed();
            verifyNoInteractions(dialog.mailService);
            verify(dialog.contactCaptcha, never()).verify(any(), any());
        }
    }
}
