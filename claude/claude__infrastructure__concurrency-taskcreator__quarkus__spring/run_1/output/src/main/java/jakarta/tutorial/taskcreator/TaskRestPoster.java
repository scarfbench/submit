package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Simple REST poster to avoid bringing in heavy client. */
@Component
public class TaskRestPoster {
    private static final Logger log = LoggerFactory.getLogger(TaskRestPoster.class);

    public void post(String msg) throws IOException {
        URL url = new URL("http://localhost:8080/taskinfo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }
        int code = conn.getResponseCode();
        if (code >= 300) {
            log.warn("Non-success response posting task update: {}", code);
        }
        conn.disconnect();
    }
}
