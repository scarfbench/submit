package spring.tutorial.fileupload;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.PartType;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

@jakarta.ws.rs.Path("/upload")
public class FileUploadController {

    private static final Logger LOGGER =
            Logger.getLogger(FileUploadController.class.getCanonicalName());

    public static class FileUploadForm {
        @FormParam("destination")
        @PartType(MediaType.TEXT_PLAIN)
        public String destination;

        @FormParam("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public InputStream file;

        @FormParam("file")
        @PartType(MediaType.TEXT_PLAIN)
        public String fileName;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response upload(@MultipartForm FileUploadForm form) {
        if (form.destination == null || form.destination.isBlank()) {
            return Response.ok("Destination must be provided").build();
        }
        if (form.file == null) {
            return Response.ok("You did not specify a file to upload.").build();
        }

        try {
            Path destDir = Paths.get(form.destination).normalize().toAbsolutePath();
            Files.createDirectories(destDir);

            String fileName = (form.fileName == null || form.fileName.isBlank())
                    ? "upload.bin"
                    : Paths.get(form.fileName).getFileName().toString();

            Path target = destDir.resolve(fileName);

            Files.copy(form.file, target, StandardCopyOption.REPLACE_EXISTING);

            LOGGER.log(Level.INFO, "File {0} being uploaded to {1}",
                    new Object[]{fileName, destDir});
            return Response.ok("New file " + fileName + " created at " + destDir).build();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Problems during file upload. Error: {0}",
                    new Object[]{ex.getMessage()});
            String msg = "You either did not specify a file to upload or are trying to upload a file to a protected or nonexistent location."
                    + "<br/> ERROR: " + ex.getMessage();
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_HTML)
                    .entity(msg)
                    .build();
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getInfo() {
        return Response.ok("Servlet that uploads files to a user-defined destination").build();
    }
}
