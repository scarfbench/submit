package spring.tutorial.fileupload;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private static final Logger LOGGER = Logger.getLogger(FileUploadController.class.getCanonicalName());

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("destination") String destination,
            @RequestParam(value = "filename", required = false) String filename) {

        String path = destination;
        String fileName = filename != null && !filename.trim().isEmpty() ? filename : file.getOriginalFilename();

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "uploaded_file";
        }

        if (path == null || path.trim().isEmpty()) {
            return ResponseEntity.ok("Please specify a destination directory for the file upload.");
        }

        try (OutputStream out = new FileOutputStream(new File(path + File.separator + fileName))) {
            out.write(file.getBytes());
            LOGGER.log(Level.INFO, "File {0} uploaded to {1}", new Object[]{fileName, path});
            return ResponseEntity.ok("New file " + fileName + " created at " + path);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Upload failed: {0}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}
