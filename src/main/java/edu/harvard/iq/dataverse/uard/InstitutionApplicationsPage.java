package edu.harvard.iq.dataverse.uard;

import edu.harvard.iq.dataverse.DataverseSession;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Named
@RequestScoped
public class InstitutionApplicationsPage {
    @EJB InstitutionApplicationService service;
    @Inject DataverseSession session;
    private String id;
    private int offset;
    private List<InstitutionApplicationService.Application> applications = List.of();
    private InstitutionApplicationService.Application application;

    public void init() throws IOException {
        var context = FacesContext.getCurrentInstance().getExternalContext();
        context.setResponseHeader("Cache-Control", "no-store");
        try {
            InstitutionApplicationService.requireAdmin(session.getUser(true));
            if (id != null && !id.isBlank()) {
                application = service.get(session.getUser(), UUID.fromString(id));
                if (application == null) fail(404);
            } else {
                applications = service.list(session.getUser(), offset);
            }
        } catch (SecurityException ex) { fail(403);
        } catch (IllegalArgumentException ex) { fail(404);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).warning("Institution application review unavailable (SQLState " + ex.getSQLState() + ")");
            fail(503);
        }
    }

    private void fail(int code) throws IOException {
        var faces = FacesContext.getCurrentInstance();
        faces.getExternalContext().responseSendError(code, "");
        faces.responseComplete();
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = Math.max(0, offset); }
    public boolean isNext() { return applications.size() > 50; }
    public List<InstitutionApplicationService.Application> getApplications() { return applications.stream().limit(50).toList(); }
    public InstitutionApplicationService.Application getApplication() { return application; }
    public List<String> getFields() { return InstitutionApplicationValidator.FIELDS; }
}
