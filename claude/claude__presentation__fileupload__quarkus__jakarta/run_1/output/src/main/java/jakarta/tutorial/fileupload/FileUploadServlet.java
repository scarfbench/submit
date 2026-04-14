package jakarta.tutorial.fileupload;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

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
            @FormDataParam("file") FormDataContentDisposition fileMetaData,
            @FormDataParam("destination") String destination) {

        String path = destination;
        String fileName = fileMetaData != null ? fileMetaData.getFileName() : "uploaded_file";

        if (path == null || path.trim().isEmpty()) {
            return Response.ok("Please specify a destination directory for the file upload.").build();
        }

        try {
            // Read file content
            byte[] fileContent = fileInputStream.readAllBytes();

            // Write to destination
            try (OutputStream out = new FileOutputStream(new File(path + File.separator + fileName))) {
                out.write(fileContent);
                LOGGER.log(Level.INFO, "File {0} uploaded to {1}", new Object[]{fileName, path});
                return Response.ok("New file " + fileName + " created at " + path).build();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Upload failed: {0}", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Upload failed: " + e.getMessage()).build();
        }
    }
}
