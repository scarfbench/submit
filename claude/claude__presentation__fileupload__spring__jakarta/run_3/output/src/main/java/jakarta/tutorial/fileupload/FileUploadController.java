package jakarta.tutorial.fileupload;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/upload")
public class FileUploadController {

    private static final Logger LOGGER =
            Logger.getLogger(FileUploadController.class.getCanonicalName());

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public Response upload(@FormDataParam("destination") String destination,
                          @FormDataParam("file") InputStream fileInputStream,
                          @FormDataParam("file") FormDataContentDisposition fileDetail) {

        if (destination == null || destination.trim().isEmpty()) {
            return Response.ok("Destination must be provided").build();
        }
        if (fileInputStream == null || fileDetail == null) {
            return Response.ok("You did not specify a file to upload.").build();
        }

        try {
            java.nio.file.Path destDir = Paths.get(destination).normalize().toAbsolutePath();
            Files.createDirectories(destDir);

            String original = fileDetail.getFileName();
            String fileName = (original == null || original.isBlank())
                    ? "upload.bin"
                    : Paths.get(original).getFileName().toString();

            java.nio.file.Path target = destDir.resolve(fileName);

            Files.copy(fileInputStream, target, StandardCopyOption.REPLACE_EXISTING);

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
