package jakarta.tutorial.fileupload;

import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.InputStream;

public class FileUploadForm {

    @FormDataParam("destination")
    public String destination;

    @FormDataParam("file")
    public InputStream file;

    @FormDataParam("filename")
    public String filename;
}
