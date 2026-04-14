/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.fileupload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * File upload controller example
 */
@Controller
public class FileUploadController {

    private final static Logger LOGGER =
            Logger.getLogger(FileUploadController.class.getCanonicalName());

    /**
     * Processes file upload requests for both HTTP GET and POST methods.
     *
     * @param destination the destination path for the uploaded file
     * @param file the uploaded file
     * @return response message
     */
    @GetMapping(value = "/upload", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String handleGetRequest(@RequestParam(required = false) String destination,
                                    @RequestParam(required = false) MultipartFile file) {
        return processRequest(destination, file);
    }

    /**
     * Handles the HTTP POST method for file uploads.
     *
     * @param destination the destination path for the uploaded file
     * @param file the uploaded file
     * @return response message
     */
    @PostMapping(value = "/upload", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String handlePostRequest(@RequestParam String destination,
                                     @RequestParam MultipartFile file) {
        return processRequest(destination, file);
    }

    /**
     * Processes the file upload request.
     *
     * @param path destination path
     * @param file uploaded file
     * @return response message
     */
    private String processRequest(String path, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "You either did not specify a file to upload or the file is empty.";
        }

        if (path == null || path.isEmpty()) {
            return "Destination path is required.";
        }

        final String fileName = file.getOriginalFilename();
        OutputStream out = null;
        InputStream filecontent = null;

        try {
            out = new FileOutputStream(new File(path + File.separator + fileName));
            filecontent = file.getInputStream();

            int read;
            final byte[] bytes = new byte[1024];

            while ((read = filecontent.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            LOGGER.log(Level.INFO, "File {0} being uploaded to {1}",
                    new Object[]{fileName, path});

            return "New file " + fileName + " created at " + path;

        } catch (FileNotFoundException fne) {
            LOGGER.log(Level.SEVERE, "Problems during file upload. Error: {0}",
                    new Object[]{fne.getMessage()});

            return "You either did not specify a file to upload or are "
                    + "trying to upload a file to a protected or nonexistent "
                    + "location.<br/> ERROR: " + fne.getMessage();
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "IO error during file upload. Error: {0}",
                    new Object[]{ioe.getMessage()});

            return "IO error occurred during file upload.<br/> ERROR: " + ioe.getMessage();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error closing output stream", e);
                }
            }
            if (filecontent != null) {
                try {
                    filecontent.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error closing input stream", e);
                }
            }
        }
    }
}
