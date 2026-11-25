package edu.harvard.iq.dataverse.uard;

import edu.harvard.iq.dataverse.DataverseSession;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/institution-application-logo")
public class InstitutionApplicationLogoServlet extends HttpServlet {
    @EJB InstitutionApplicationService applications;
    @Inject DataverseSession session;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        try {
            InstitutionApplicationService.requireAdmin(session.getUser(true));
            var logo = applications.logo(session.getUser(), UUID.fromString(request.getParameter("id")));
            if (logo == null) { response.sendError(404); return; }
            response.setContentType(logo.type());
            response.setHeader("Content-Disposition", "attachment; filename=\"logo." + logo.type().substring(6) + "\"");
            response.setContentLength(logo.bytes().length);
            response.getOutputStream().write(logo.bytes());
        } catch (SecurityException ex) { response.sendError(403);
        } catch (IllegalArgumentException | NullPointerException ex) { response.sendError(404);
        } catch (SQLException ex) { response.sendError(503); }
    }
}
