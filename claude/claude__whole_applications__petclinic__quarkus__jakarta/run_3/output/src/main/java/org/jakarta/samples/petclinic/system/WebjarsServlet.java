package org.jakarta.samples.petclinic.system;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serves WebJar resources from the classpath.
 * Maps /webjars/* URLs to META-INF/resources/webjars/* on the classpath.
 */
@WebServlet("/webjars/*")
public class WebjarsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || path.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String resourcePath = "META-INF/resources/webjars" + path;
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);

        if (is == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Set content type based on file extension
        String contentType = getServletContext().getMimeType(path);
        if (contentType == null) {
            if (path.endsWith(".css")) {
                contentType = "text/css";
            } else if (path.endsWith(".js")) {
                contentType = "application/javascript";
            } else if (path.endsWith(".woff")) {
                contentType = "font/woff";
            } else if (path.endsWith(".woff2")) {
                contentType = "font/woff2";
            } else if (path.endsWith(".ttf")) {
                contentType = "font/ttf";
            } else if (path.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else if (path.endsWith(".eot")) {
                contentType = "application/vnd.ms-fontobject";
            } else {
                contentType = "application/octet-stream";
            }
        }
        resp.setContentType(contentType);

        try (OutputStream out = resp.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            is.close();
        }
    }
}
