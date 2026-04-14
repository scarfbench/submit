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
 * File upload controller for Spring Boot
 */
@Controller
public class FileUploadController {

    private final static Logger LOGGER =
            Logger.getLogger(FileUploadController.class.getCanonicalName());

    /**
     * Handles file upload requests
     *
     * @param file the uploaded file
     * @param destination the destination path
     * @return response message
     */
    @PostMapping(value = "/upload", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("destination") String destination) {

        if (file.isEmpty()) {
            return "You either did not specify a file to upload or are "
                    + "trying to upload a file to a protected or nonexistent "
                    + "location.";
        }

        final String fileName = file.getOriginalFilename();
        OutputStream out = null;
        InputStream filecontent = null;

        try {
            out = new FileOutputStream(new File(destination + File.separator + fileName));
            filecontent = file.getInputStream();

            int read;
            final byte[] bytes = new byte[1024];

            while ((read = filecontent.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            LOGGER.log(Level.INFO, "File {0} being uploaded to {1}",
                    new Object[]{fileName, destination});

            return "New file " + fileName + " created at " + destination;

        } catch (FileNotFoundException fne) {
            LOGGER.log(Level.SEVERE, "Problems during file upload. Error: {0}",
                    new Object[]{fne.getMessage()});

            return "You either did not specify a file to upload or are "
                    + "trying to upload a file to a protected or nonexistent "
                    + "location.<br/> ERROR: " + fne.getMessage();

        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Problems during file upload. Error: {0}",
                    new Object[]{ioe.getMessage()});

            return "Error during file upload: " + ioe.getMessage();

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (filecontent != null) {
                    filecontent.close();
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error closing streams: {0}", e.getMessage());
            }
        }
    }

    /**
     * Handles GET requests to upload endpoint
     *
     * @param file the uploaded file
     * @param destination the destination path
     * @return response message
     */
    @GetMapping(value = "/upload", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String handleGetRequest(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "destination", required = false) String destination) {
        if (file != null && destination != null) {
            return handleFileUpload(file, destination);
        }
        return "Please use POST method for file upload";
    }
}
