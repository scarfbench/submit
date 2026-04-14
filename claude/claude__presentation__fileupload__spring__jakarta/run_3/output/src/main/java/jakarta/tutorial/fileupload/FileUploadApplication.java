package jakarta.tutorial.fileupload;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class FileUploadApplication extends Application {
    // JAX-RS application configuration
    // Jersey will automatically discover and register resource classes
}
