package edu.harvard.iq.dataverse.filter;

import edu.harvard.iq.dataverse.UardApiUi;
import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = {
    "/misc/tou3_uk.html", "/misc/tou3_en.html",
    "/datatags-api-test.xhtml", "/datatags-api-test-deposit-complete.xhtml",
    "/datatags-api-test.jsf", "/datatags-api-test-deposit-complete.jsf",
    "/datatags-api-test.faces", "/datatags-api-test-deposit-complete.faces",
    "/faces/datatags-api-test.xhtml", "/faces/datatags-api-test-deposit-complete.xhtml"
})
public class UardApiUiPageFilter extends HttpFilter {

    @Inject
    UardApiUi uardApiUi;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (uardApiUi.isLaunchRestricted()) {
            response.setHeader("Cache-Control", "no-store");
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(request, response);
    }
}
