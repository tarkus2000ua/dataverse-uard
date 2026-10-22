package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.branding.BrandingUtilTest;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import edu.harvard.iq.dataverse.util.BundleUtil;
import edu.harvard.iq.dataverse.util.SystemConfig;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PublicationEmailLocaleTest {
    @TempDir Path bundles;
    private String previousDirectory;
    private MailServiceBean mail;
    private final Locale originalDefault = Locale.getDefault();

    @BeforeEach
    void setup() throws Exception {
        previousDirectory = System.getProperty("dataverse.lang.directory");
        Files.writeString(bundles.resolve("Bundle.properties"), properties("Hello", "Published", "Publish failed", "Access granted", "Thank you", "Support"));
        Files.writeString(bundles.resolve("Bundle_uk.properties"), properties("Привіт", "Опубліковано", "Помилка публікації", "Доступ надано", "Дякуємо", "Підтримка"));
        Files.copy(bundles.resolve("Bundle_uk.properties"), bundles.resolve("Bundle_ua.properties"));
        System.setProperty("dataverse.lang.directory", bundles.toString());
        // Initialize the cached loader before concurrent bundle reads.
        BundleUtil.getResourceBundle("Bundle", Locale.ENGLISH);
        BrandingUtilTest.setupMocks();
        BrandingUtilTest.setInstallationName("UaRD");
        BrandingUtilTest.setRootDataverseName("UaRD");

        Dataset dataset = mock(Dataset.class, RETURNS_DEEP_STUBS);
        when(dataset.getDisplayName()).thenReturn("Test dataset");
        when(dataset.getGlobalId().asString()).thenReturn("doi:10.5072/TEST");
        when(dataset.getOwner().getDisplayName()).thenReturn("Test collection");
        when(dataset.getOwner().getAlias()).thenReturn("uard");
        DatasetVersion version = mock(DatasetVersion.class);
        when(version.getDataset()).thenReturn(dataset);
        InternetAddress address = new InternetAddress("support@example.invalid");
        mail = new MailServiceBean(Session.getInstance(new Properties()), mock(SettingsServiceBean.class)) {
            @Override public Optional<InternetAddress> getSystemAddress() { return Optional.of(address); }
            @Override public Optional<InternetAddress> getSupportAddress() { return Optional.of(address); }
            @Override public Object getObjectOfNotification(UserNotification notification) {
                return notification.getType() == UserNotification.Type.GRANTFILEACCESS ? dataset : version;
            }
        };
        mail.systemConfig = mock(SystemConfig.class);
        when(mail.systemConfig.getDataverseSiteUrl()).thenReturn("https://example.invalid");
    }

    @AfterEach
    void cleanup() {
        if (previousDirectory == null) System.clearProperty("dataverse.lang.directory");
        else System.setProperty("dataverse.lang.directory", previousDirectory);
        BrandingUtilTest.tearDownMocks();
        assertEquals(originalDefault, Locale.getDefault());
    }

    private String properties(String hello, String published, String failed, String granted, String thanks, String support) {
        return "notification.email.greeting=" + hello + "\\n\n"
                + "notification.email.publish.dataset.subject={0}: " + published + " {1}\n"
                + "notification.email.wasPublished=" + published + " {0} {1} {2} {3}\n"
                + "notification.email.publishFailure.dataset.subject={0}: " + failed + " {1}\n"
                + "notification.email.publishFailedPidReg=" + failed + " {0} {1} {2} {3}\n"
                + "notification.email.grant.file.access.subject={0}: " + granted + "\n"
                + "notification.email.grantFileAccess=" + granted + " {0} {1}\n"
                + "notification.email.closing=\\n" + thanks + " {0} {1}\n"
                + "notification.email.closing.html=<br>" + thanks + " {0} {1}\n"
                + "contact.support=" + support + "\n";
    }

    private CompletableFuture<MimeMessage> sendInBackground(Locale locale, boolean html, UserNotification.Type type) {
        return CompletableFuture.supplyAsync(() -> {
            // Static mocks are thread-local: install the SMTP interception in the worker.
            try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
                AtomicReference<MimeMessage> captured = new AtomicReference<>();
                transport.when(() -> Transport.send(any(Message.class), any(Address[].class)))
                        .thenAnswer(call -> {
                            MimeMessage message = call.getArgument(0);
                            // Transport.send normally finalizes MIME headers before delivery.
                            message.saveChanges();
                            captured.set(message);
                            return null;
                        });
                AuthenticatedUser recipient = new AuthenticatedUser();
                recipient.setEmail("recipient@example.invalid");
                UserNotification notification = new UserNotification();
                notification.setUser(recipient);
                notification.setType(type);
                boolean sent = locale == null
                        ? mail.sendNotificationEmail(notification, "", null, html)
                        : mail.sendNotificationEmail(notification, "", null, html, locale);
                assertTrue(sent);
                assertNotNull(captured.get());
                return captured.get();
            }
        });
    }

    @ParameterizedTest
    @CsvSource({"uk,false", "ua,false", "en,false", "uk,true", "en,true"})
    void publicationMailUsesOneLocaleForSubjectBodyAndSignatureWithoutJsf(String language, boolean html) throws Exception {
        MimeMessage message = sendInBackground(Locale.forLanguageTag(language), html, UserNotification.Type.PUBLISHEDDS).get(30, TimeUnit.SECONDS);
        boolean english = language.equals("en");
        assertEquals("UaRD: " + (english ? "Published" : "Опубліковано") + " Test dataset", message.getSubject());
        String body = message.getContent().toString();
        assertTrue(body.startsWith(english ? "Hello" : "Привіт"));
        assertTrue(body.contains("https://example.invalid/dataset.xhtml?persistentId=doi:10.5072/TEST"));
        assertTrue(body.contains((html ? "<br>" : "\n") + (english ? "Thank you" : "Дякуємо")));
        assertTrue(body.contains(english ? "UaRD Support" : "UaRD Підтримка"));
        assertTrue(message.isMimeType(html ? "text/html" : "text/plain"));
    }

    @Test
    void concurrentEnglishAndUkrainianEmailsDoNotChangeEachOthersLanguage() throws Exception {
        var ukrainian = sendInBackground(Locale.forLanguageTag("uk"), false, UserNotification.Type.PUBLISHEDDS);
        var english = sendInBackground(Locale.ENGLISH, false, UserNotification.Type.PUBLISHEDDS);
        CompletableFuture.allOf(ukrainian, english).get(30, TimeUnit.SECONDS);
        assertTrue(ukrainian.get().getContent().toString().contains("Дякуємо"));
        assertFalse(ukrainian.get().getContent().toString().contains("Thank you"));
        assertTrue(english.get().getContent().toString().contains("Thank you"));
        assertFalse(english.get().getContent().toString().contains("Дякуємо"));
    }

    @ParameterizedTest
    @CsvSource({"PUBLISHFAILED_PIDREG,Помилка публікації", "GRANTFILEACCESS,Доступ надано"})
    void relatedPublicationNotificationsUseTheCapturedLocale(UserNotification.Type type, String expected) throws Exception {
        MimeMessage message = sendInBackground(Locale.forLanguageTag("uk"), false, type).get(30, TimeUnit.SECONDS);
        assertTrue(message.getSubject().contains(expected));
        assertTrue(message.getContent().toString().contains(expected));
    }

    @Test
    void missingCapturedLocaleKeepsExistingBackgroundFallback() throws Exception {
        MimeMessage message = sendInBackground(null, false, UserNotification.Type.PUBLISHEDDS).get(30, TimeUnit.SECONDS);
        assertEquals("UaRD: Published Test dataset", message.getSubject());
    }
}
