package spring.tutorial.fileupload;

import org.springframework.http.HttpStatus;
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

    @PostMapping(consumes = "multipart/form-data", produces = "text/html")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "destination", required = false) String destination,
            @RequestParam(value = "filename", required = false) String filename) {

        String path = destination;
        String fileName = (filename != null && !filename.trim().isEmpty()) ? filename : file.getOriginalFilename();

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "uploaded_file";
        }

        if (path == null || path.trim().isEmpty()) {
            return ResponseEntity.ok("Please specify a destination directory for the file upload.");
        }

        try {
            byte[] fileContent = file.getBytes();
            File targetFile = new File(path + File.separator + fileName);

            try (OutputStream out = new FileOutputStream(targetFile)) {
                out.write(fileContent);
                LOGGER.log(Level.INFO, "File {0} uploaded to {1}", new Object[]{fileName, path});
                return ResponseEntity.ok("New file " + fileName + " created at " + path);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Upload failed: {0}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}
