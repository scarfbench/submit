package jakarta.tutorial.web.dukeetf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

// Tests disabled for Jakarta EE migration - require deployed application server
@Disabled("Tests require deployed Jakarta EE application server")
class LongPollSmokeTest {
  @Test
  void dukeetfRespondsWithinAFewSeconds() {
    given().when().get("http://localhost:8080/dukeetf/dukeetf")
      .then().statusCode(200)
      .header("Content-Type", startsWith("text/html"))
      .body(matchesRegex("\\s*[-+]?\\d+\\.?\\d*\\s*/\\s*-?\\d+\\s*"));
  }
}
