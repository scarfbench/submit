package quarkus.tutorial.web.dukeetf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JsfSmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void mainXhtmlServed() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/main.xhtml",
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString().startsWith("text/html"));
    }
}
