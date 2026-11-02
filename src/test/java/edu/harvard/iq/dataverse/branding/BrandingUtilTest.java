package edu.harvard.iq.dataverse.branding;

import edu.harvard.iq.dataverse.DataverseServiceBean;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import edu.harvard.iq.dataverse.util.BundleUtil;
import java.io.UnsupportedEncodingException;
import java.io.StringReader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Stream;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

public class BrandingUtilTest {
    
    static DataverseServiceBean dataverseSvc;
    static SettingsServiceBean settingsSvc;
    static final String DEFAULT_NAME = "LibraScholar";
    
    final Logger log = Logger.getLogger(this.getClass().getCanonicalName());
    
    /**
     * Create default mocks, reusable for other tests.
     * Each call will create new, fresh mocks. (So this should ensure atomic tests to some degree...)
     */
    @BeforeAll
    public static void setupMocks() {
        dataverseSvc = Mockito.mock(DataverseServiceBean.class);
        settingsSvc = Mockito.mock(SettingsServiceBean.class);
        BrandingUtil.injectServices(dataverseSvc, settingsSvc);
        
        // initial values (needed here for other tests where this method is reused!)
        Mockito.lenient().when(settingsSvc.getValueForKey(SettingsServiceBean.Key.InstallationName)).thenReturn(DEFAULT_NAME);
        Mockito.lenient().when(dataverseSvc.getRootDataverseName()).thenReturn(DEFAULT_NAME);
    }
    
    /**
     * Override default mocked value
     * @param installationName
     */
    public static void setInstallationName(String installationName) {
        Mockito.lenient().when(settingsSvc.getValueForKey(SettingsServiceBean.Key.InstallationName)).thenReturn(installationName);
    }
    
    /**
     * Override default mocked name
     * @param rootDataverseName
     */
    public static void setRootDataverseName(String rootDataverseName) {
        Mockito.lenient().when(dataverseSvc.getRootDataverseName()).thenReturn(rootDataverseName);
    }
    
    /**
     * After using, please free the mocks. Tests need atomicity.
     */
    @AfterAll
    public static void tearDownMocks() {
        BrandingUtil.injectServices(null, null);
    }
    
    /**
     * Reset to default values before each test, trying to provide atomicity.
     */
    @BeforeEach
    void setDefaultMockValues() {
        Mockito.when(settingsSvc.getValueForKey(SettingsServiceBean.Key.InstallationName)).thenReturn(DEFAULT_NAME);
        Mockito.when(dataverseSvc.getRootDataverseName()).thenReturn(DEFAULT_NAME);
    }
    
    @ParameterizedTest
    @CsvSource(value ={
        "NULL, " + DEFAULT_NAME, // (Defaults to root collection name)
        "NotLibraScholar, NotLibraScholar"
    }, nullValues = {"NULL"})
    public void testGetInstallationBrandName(String mockedInstallationName, String expected) {
        // given
        Mockito.when(settingsSvc.getValueForKey(SettingsServiceBean.Key.InstallationName)).thenReturn(mockedInstallationName);
        // when & then
        assertEquals(expected, BrandingUtil.getInstallationBrandName());
    }
    
    static Stream<Arguments> supportTeamName() throws UnsupportedEncodingException, AddressException {
        // expected string, InternetAddress
        return Stream.of(
            Arguments.of(null, "Support", null),
            Arguments.of("", "Support", null),
            Arguments.of(DEFAULT_NAME, DEFAULT_NAME + " Support", null),
            Arguments.of(DEFAULT_NAME, DEFAULT_NAME + " Support", new InternetAddress("support@librascholar.edu")),
            Arguments.of(DEFAULT_NAME, "LibraScholar Support Team", new InternetAddress("support@librascholar.edu", "LibraScholar Support Team")),
            // misconfiguration to set to empty string
            Arguments.of(DEFAULT_NAME, "", new InternetAddress("support@librascholar.edu", ""))
        );
    }
    
    @ParameterizedTest
    @MethodSource("supportTeamName")
    public void testGetSupportTeamName(String mockedRootName, String expected, InternetAddress email) throws AddressException, UnsupportedEncodingException {
        // given
        Mockito.when(dataverseSvc.getRootDataverseName()).thenReturn(mockedRootName);
        // when & then
        assertEquals(expected, BrandingUtil.getSupportTeamName(email));
    }

    @Test
    public void testUkrainianGeneratedSupportNameAndHeader() throws Exception {
        ResourceBundle bundle = ukrainianSupportBundle();
        assertEquals("Підтримка " + DEFAULT_NAME, BrandingUtil.getSupportTeamName(null, bundle));
        assertEquals("Зв’язатися з підтримкою " + DEFAULT_NAME, BrandingUtil.getContactHeader(null, bundle));
        InternetAddress address = new InternetAddress("support@example.org");
        assertEquals("Зв’язатися з підтримкою " + DEFAULT_NAME, BrandingUtil.getContactHeader(address, bundle));
    }

    @Test
    public void testUkrainianConfiguredSupportNameTakesPrecedence() throws Exception {
        ResourceBundle bundle = ukrainianSupportBundle();
        InternetAddress address = new InternetAddress("support@example.org", "Команда UaRD");
        assertEquals("Команда UaRD", BrandingUtil.getSupportTeamName(address, bundle));
        assertEquals("Зв’язатися з Команда UaRD", BrandingUtil.getContactHeader(address, bundle));
        address.setPersonal("");
        assertEquals("", BrandingUtil.getSupportTeamName(address, bundle));
        assertEquals("Зв’язатися з ", BrandingUtil.getContactHeader(address, bundle));
    }

    @Test
    public void testUkrainianSupportWithoutRootName() throws Exception {
        ResourceBundle bundle = ukrainianSupportBundle();
        for (String rootName : new String[] {null, ""}) {
            Mockito.when(dataverseSvc.getRootDataverseName()).thenReturn(rootName);
            assertEquals("Підтримка", BrandingUtil.getSupportTeamName(null, bundle));
            assertEquals("Зв’язатися з Підтримка", BrandingUtil.getContactHeader(null, bundle));
        }
    }

    private static ResourceBundle ukrainianSupportBundle() throws Exception {
        return new PropertyResourceBundle(new StringReader(
                "contact.header=Зв’язатися з {0}\n"
                + "contact.support=Підтримка\n"
                + "dataverse=Колекція\n"
                + "org.support.team.root=Підтримка {0}\n"
                + "org.contact.header.root=Зв’язатися з підтримкою {0}\n"));
    }

    static Stream<Arguments> supportEmailAddress() throws UnsupportedEncodingException, AddressException {
        // expected string, InternetAddress
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of("support@librascholar.edu", new InternetAddress("support@librascholar.edu")),
            Arguments.of("support@librascholar.edu", new InternetAddress("support@librascholar.edu", "LibraScholar")),
            // misconfiguration to set to empty string but doesn't matter
            Arguments.of("support@librascholar.edu", new InternetAddress("support@librascholar.edu", "")),
            // misconfiguration to set to null
            Arguments.of(null, new InternetAddress(null, "LibraScholar Support Team")),
            // misconfiguration to set to empty string
            Arguments.of("", new InternetAddress("", "LibraScholar Support Team"))
        );
    }
    
    @ParameterizedTest
    @MethodSource("supportEmailAddress")
    public void testGetSupportEmailAddress(String expected, InternetAddress email) {
        assertEquals(expected, BrandingUtil.getSupportTeamEmailAddress(email));
    }

    @Test
    public void testWelcomeInAppNotification(TestInfo testInfo) {
        log.fine(testInfo.getDisplayName());
        String message = BundleUtil.getStringFromBundle("notification.welcome",
                Arrays.asList(
                        "LibraScholar",
                        "<a href=\"http://guides.dataverse.org/en/4.3/user/index.html\">User Guide</a>",
                        "<a href=\"https://demo.dataverse.org\">Demo Site</a>"
                ))
                + " " + BundleUtil.getStringFromBundle("notification.welcomeConfirmEmail");
        log.fine("message: " + message);
        assertEquals("Welcome to LibraScholar! Get started by adding or finding data. "
                + "Have questions? Check out the <a href=\"http://guides.dataverse.org/en/4.3/user/index.html\">User Guide</a>."
                + " Want to test out Dataverse features? Use our <a href=\"https://demo.dataverse.org\">Demo Site</a>."
                + " Also, check for your welcome email to verify your address.",
                message);
    }

    @Test
    public void testWelcomeEmail(TestInfo testInfo) {
        log.fine(testInfo.getDisplayName());
        String message = BundleUtil.getStringFromBundle("notification.email.welcome",
                Arrays.asList(
                        "LibraScholar",
                        "http://guides.librascholar.edu/en",
                        "4.3",
                        "LibraScholar Support",
                        "support@librascholar.edu"
                ));
        log.fine("message: " + message);
        assertEquals("Welcome to LibraScholar! Get started by adding or finding data. "
                + "Have questions? Check out the User Guide at http://guides.librascholar.edu/en/4.3/user or"
                + " contact LibraScholar Support at support@librascholar.edu for assistance.",
                message);
    }

    @Test
    public void testEmailClosing(TestInfo testInfo) {
        log.fine(testInfo.getDisplayName());
        String message = BundleUtil.getStringFromBundle("notification.email.closing",
                Arrays.asList(
                        "support@librascholar.edu",
                        "LibraScholar Support Team"
                ));
        log.fine("message: " + message);
        assertEquals("\n\nYou may contact us for support at support@librascholar.edu.\n\nThank you,\nLibraScholar Support Team",
                message);
    }

    @Test
    public void testEmailSubject(TestInfo testInfo) {
        log.fine(testInfo.getDisplayName());
        String message = BundleUtil.getStringFromBundle("notification.email.create.account.subject",
                Arrays.asList(
                        "LibraScholar"
                ));
        log.fine("message: " + message);
        assertEquals("LibraScholar: Your account has been created",
                message);
    }

    @Test
    public void testGetContactHeader() {
        Mockito.when(dataverseSvc.getRootDataverseName()).thenReturn(null);
        assertEquals("Contact Support", BrandingUtil.getContactHeader(null));
    }

}
