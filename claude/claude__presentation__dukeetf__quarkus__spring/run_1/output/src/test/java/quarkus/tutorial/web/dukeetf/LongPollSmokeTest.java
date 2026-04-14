package quarkus.tutorial.web.dukeetf;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LongPollSmokeTest {

  @LocalServerPort
  private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @Test
  void dukeetfRespondsWithinAFewSeconds() {
    given().when().get("/dukeetf")
      .then().statusCode(200)
      .header("Content-Type", startsWith("text/html"))
      .body(matchesRegex("\\s*[-+]?\\d+\\.?\\d*\\s*/\\s*-?\\d+\\s*"));
  }
}
