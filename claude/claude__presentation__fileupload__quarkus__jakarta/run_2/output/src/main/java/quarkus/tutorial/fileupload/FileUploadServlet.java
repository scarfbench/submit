package quarkus.tutorial.fileupload;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "FileUploadServlet", urlPatterns = {"/upload"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10,  // 10 MB
    maxRequestSize = 1024 * 1024 * 15 // 15 MB
)
public class FileUploadServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getCanonicalName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Get form parameters
        String path = request.getParameter("destination");
        String fileName = request.getParameter("filename");

        // Get the uploaded file part
        Part filePart = request.getPart("file");

        // Validation
        if (path == null || path.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Please specify a destination directory for the file upload.");
            return;
        }

        if (filePart == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("No file uploaded.");
            return;
        }

        // Use submitted filename or extract from part
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = getSubmittedFileName(filePart);
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "uploaded_file";
            }
        }

        try {
            // Create destination directory if it doesn't exist
            File destDir = new File(path);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            // Write file to destination
            File destFile = new File(destDir, fileName);
            try (InputStream fileContent = filePart.getInputStream();
                 OutputStream fileOut = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fileContent.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, bytesRead);
                }
            }

            LOGGER.log(Level.INFO, "File {0} uploaded to {1}", new Object[]{fileName, path});
            response.setStatus(HttpServletResponse.SC_OK);
            out.println("New file " + fileName + " created at " + path);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Upload failed: {0}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Upload failed: " + e.getMessage());
        }
    }

    private String getSubmittedFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String token : contentDisposition.split(";")) {
                if (token.trim().startsWith("filename")) {
                    return token.substring(token.indexOf('=') + 1).trim()
                            .replace("\"", "");
                }
            }
        }
        return null;
    }
}
