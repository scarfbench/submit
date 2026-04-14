package spring.tutorial.web.dukeetf;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.startsWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JsfSmokeTest {

    @LocalServerPort
    private int port;

    @Test
    void mainXhtmlServed() {
        given()
            .port(port)
            .when()
            .get("/main.xhtml")
            .then()
            .statusCode(200)
            .header("Content-Type", startsWith("application/xhtml+xml"));
    }
}
