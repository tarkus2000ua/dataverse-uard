package edu.harvard.iq.dataverse.uard;

import edu.harvard.iq.dataverse.MailServiceBean;
import edu.harvard.iq.dataverse.util.SystemConfig;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.DependsOn;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

/** Durable retries; the singleton's write lock serializes scheduled and immediate delivery. */
@Singleton
@DependsOn("StartupFlywayMigrator")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class InstitutionApplicationNotifier {
    private static final Logger LOGGER = Logger.getLogger(InstitutionApplicationNotifier.class.getName());
    @EJB InstitutionApplicationService applications;
    @EJB MailServiceBean mail;
    @EJB SystemConfig config;

    @Asynchronous
    public void wake() { deliver(); }

    @Schedule(hour = "*", minute = "*/1", second = "30", persistent = false)
    public void deliver() {
        try {
            for (UUID id : applications.pending()) {
                boolean sent = false;
                try {
                    var recipient = mail.getSupportAddress();
                    if (recipient.isPresent()) {
                        // Personal data and logos are accessible only after native administrator login.
                        sent = mail.sendSystemEmail(recipient.get().getAddress(), "UaRD: нова заявка закладу / New institution application",
                                "Заявку закладу збережено / An institution application has been saved.\n\n"
                                + "ID: " + id + "\n"
                                + config.getDataverseSiteUrl() + "/institution-applications.xhtml?id=" + id);
                    }
                } catch (RuntimeException ex) {
                    LOGGER.warning("Institution application notification failed: " + id);
                }
                applications.notificationResult(id, sent);
            }
        } catch (SQLException ex) {
            LOGGER.warning("Institution application notification queue is unavailable (SQLState " + ex.getSQLState() + ")");
        } catch (RuntimeException ex) {
            // Keep the timer alive when a dependency is temporarily unavailable during startup/redeployment.
            LOGGER.warning("Institution application notification queue temporarily unavailable");
        }
    }
}
