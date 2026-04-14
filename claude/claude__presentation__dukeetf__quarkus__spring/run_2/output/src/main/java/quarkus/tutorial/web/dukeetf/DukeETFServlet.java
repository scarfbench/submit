package quarkus.tutorial.web.dukeetf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/dukeetf")
public class DukeETFServlet {

    private static final Logger logger = Logger.getLogger("DukeETFResource");

    @Autowired
    PriceVolumeService service;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public CompletableFuture<ResponseEntity<String>> getETFData() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

        service.register(future);

        logger.log(Level.INFO, "Connection open (queued).");

        return future;
    }
}
