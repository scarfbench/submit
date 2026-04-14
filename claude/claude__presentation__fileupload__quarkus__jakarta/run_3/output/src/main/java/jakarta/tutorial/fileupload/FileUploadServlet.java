package jakarta.tutorial.fileupload;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/upload")
public class FileUploadServlet {

    private static final Logger LOGGER = Logger.getLogger(FileUploadServlet.class.getCanonicalName());

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response uploadFile(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("filename") String filename,
            @FormDataParam("destination") String destination) {

        String path = destination;
        String fileName = filename != null ? filename : "uploaded_file";

        if (path == null || path.trim().isEmpty()) {
            return Response.ok("Please specify a destination directory for the file upload.").build();
        }

        try (OutputStream out = new FileOutputStream(new File(path + File.separator + fileName))) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            LOGGER.log(Level.INFO, "File {0} uploaded to {1}", new Object[]{fileName, path});
            return Response.ok("New file " + fileName + " created at " + path).build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Upload failed: {0}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Upload failed: " + e.getMessage()).build();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to close input stream: {0}", e.getMessage());
            }
        }
    }
}
