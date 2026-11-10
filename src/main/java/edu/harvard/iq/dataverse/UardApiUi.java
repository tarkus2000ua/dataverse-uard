package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean.Key;
import edu.harvard.iq.dataverse.util.SystemConfig;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@Named
@RequestScoped
public class UardApiUi {

    @EJB
    SettingsServiceBean settings;

    @EJB
    SystemConfig systemConfig;

    @Inject
    DataverseSession session;

    private Boolean launchRestricted;

    public boolean isLaunchRestricted() {
        if (launchRestricted == null) {
            launchRestricted = settings.isTrueForKey(Key.UardHidePublicApiUi, true);
        }
        return launchRestricted;
    }

    public boolean isVisible() {
        return !isLaunchRestricted() || session.getUser().isSuperuser();
    }

    public String getRegistrationTerms() {
        String terms = systemConfig.getApplicationTermsOfUse();
        if (!isLaunchRestricted() || terms == null) {
            return terms;
        }

        Document document = Jsoup.parseBodyFragment(terms);
        Elements apiItems = document.select("li:has(a[href='/misc/tou3_uk.html']), li:has(a[href='/misc/tou3_en.html'])");
        if (apiItems.isEmpty()) {
            return terms;
        }
        apiItems.remove();
        document.outputSettings().prettyPrint(false);
        return document.body().html();
    }
}
