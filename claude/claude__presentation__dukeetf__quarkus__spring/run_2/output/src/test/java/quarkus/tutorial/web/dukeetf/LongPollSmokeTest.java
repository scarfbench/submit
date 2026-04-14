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
class LongPollSmokeTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void dukeetfRespondsWithinAFewSeconds() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/dukeetf",
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getContentType().toString().startsWith("text/html"));

        // Verify body matches pattern: number / number
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.matches("\\s*[-+]?\\d+\\.?\\d*\\s*/\\s*-?\\d+\\s*"),
                "Response body should match pattern 'number / number', got: " + body);
    }
}
