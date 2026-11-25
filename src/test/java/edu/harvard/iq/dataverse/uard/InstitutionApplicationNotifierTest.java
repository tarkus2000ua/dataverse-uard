package edu.harvard.iq.dataverse.uard;

import edu.harvard.iq.dataverse.MailServiceBean;
import edu.harvard.iq.dataverse.util.SystemConfig;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class InstitutionApplicationNotifierTest {
    @Test void marksDeliveryOnlyAfterSuccessAndLeavesFailuresForRetry() throws Exception {
        var worker = new InstitutionApplicationNotifier();
        worker.applications = mock(InstitutionApplicationService.class);
        worker.mail = mock(MailServiceBean.class);
        worker.config = mock(SystemConfig.class);
        UUID id = UUID.randomUUID();
        when(worker.applications.pending()).thenReturn(List.of(id));
        when(worker.mail.getSupportAddress()).thenReturn(Optional.of(new InternetAddress("support@example.org")));
        when(worker.config.getDataverseSiteUrl()).thenReturn("https://example.org");
        when(worker.mail.sendSystemEmail(anyString(), anyString(), anyString())).thenReturn(false, true);
        worker.deliver();
        verify(worker.applications).notificationResult(id, false);
        worker.deliver();
        verify(worker.applications).notificationResult(id, true);
        verify(worker.mail, times(2)).sendSystemEmail(eq("support@example.org"), anyString(),
                contains("https://example.org/institution-applications.xhtml?id=" + id));
    }
    @Test void missingRecipientOrMailExceptionKeepsNotificationPending() throws Exception {
        var worker = new InstitutionApplicationNotifier();
        worker.applications = mock(InstitutionApplicationService.class);
        worker.mail = mock(MailServiceBean.class);
        worker.config = mock(SystemConfig.class);
        UUID id = UUID.randomUUID();
        when(worker.applications.pending()).thenReturn(List.of(id));
        when(worker.mail.getSupportAddress()).thenReturn(Optional.empty());
        worker.deliver();
        verify(worker.applications).notificationResult(id, false);
        verify(worker.mail, never()).sendSystemEmail(anyString(), anyString(), anyString());
        when(worker.mail.getSupportAddress()).thenThrow(new IllegalStateException());
        worker.deliver();
        verify(worker.applications, times(2)).notificationResult(id, false);
    }
}
