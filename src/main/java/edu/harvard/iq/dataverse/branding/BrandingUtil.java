package edu.harvard.iq.dataverse.branding;

import edu.harvard.iq.dataverse.DataverseServiceBean;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import edu.harvard.iq.dataverse.util.BundleUtil;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import jakarta.mail.internet.InternetAddress;

public class BrandingUtil {

    private static final Logger logger = Logger.getLogger(BrandingUtil.class.getCanonicalName());
    
    private static DataverseServiceBean dataverseService;
    private static SettingsServiceBean settingsService;

    public static String getInstallationBrandName() {
        String brandName = settingsService.getValueForKey(SettingsServiceBean.Key.InstallationName);
        // Separate if statement simplifies test setup, otherwise could use the getValueForKey method with a default param
        if (brandName == null) {
            brandName = dataverseService.getRootDataverseName();
        }
        return brandName;
    }

    // Convenience to access root name without injecting dataverseService (e.g. in
    // DatasetVersion)
    public static String getRootDataverseCollectionName() {
        return dataverseService.getRootDataverseName();
    }

    public static String getSupportTeamName(InternetAddress systemAddress) {
        return getSupportTeamName(systemAddress, BundleUtil.getResourceBundle("Bundle"));
    }

    public static String getSupportTeamName(InternetAddress systemAddress, ResourceBundle bundle) {
        if (systemAddress != null) {
            String personalName = systemAddress.getPersonal();
            if (personalName != null) {
                return personalName;
            }
        }
        String rootDataverseName = dataverseService.getRootDataverseName();
        if (rootDataverseName != null && !rootDataverseName.isEmpty()) {
            return rootDataverseName + " " + BundleUtil.getStringFromBundle("contact.support", null, bundle);
        }
        String saneDefault = BundleUtil.getStringFromBundle("dataverse", null, bundle);
        return BundleUtil.getStringFromBundle("contact.support", Arrays.asList(saneDefault), bundle);
    }

    public static String getSupportTeamEmailAddress(InternetAddress systemAddress) {
        if (systemAddress == null) {
            return null;
        }
        return systemAddress.getAddress();
    }

    public static String getContactHeader(InternetAddress systemAddress) {
        return BundleUtil.getStringFromBundle("contact.header", Arrays.asList(getSupportTeamName(systemAddress)));
    }

    public static void injectServices(DataverseServiceBean dataverseSvc, SettingsServiceBean settingsSvc) {
        dataverseService = dataverseSvc;
        settingsService = settingsSvc;
    }
}
