package quarkus.tutorial.web.dukeetf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/dukeetf")
public class DukeETFServlet {

    private static final Logger logger = Logger.getLogger("DukeETFResource");

    @Autowired
    private PriceVolumeService service;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public DeferredResult<ResponseEntity<String>> getETFData() {
        DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>();

        service.register(deferredResult);

        logger.log(Level.INFO, "Connection open (queued).");

        return deferredResult;
    }
}
