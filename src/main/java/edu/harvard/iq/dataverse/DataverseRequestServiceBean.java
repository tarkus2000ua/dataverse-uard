package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * The service bean to go to when one needs the current {@link DataverseRequest}.
 * @author michael
 */
@Named
@RequestScoped
public class DataverseRequestServiceBean {
    
    @Inject
    DataverseSession dataverseSessionSvc;
    
    @Inject
    private HttpServletRequest request;
    
   private DataverseRequest dataverseRequest;
    
    @PostConstruct
    protected void setup() {
        Locale notificationLocale = Locale.forLanguageTag(dataverseSessionSvc.getLocaleCode().replace('_', '-'));
        dataverseRequest = new DataverseRequest(dataverseSessionSvc.getUser(), request, notificationLocale);
    }
    
    public DataverseRequest getDataverseRequest() {
        return dataverseRequest;
    }
    
}
