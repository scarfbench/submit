package jakarta.tutorial.web.dukeetf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.startsWith;

// Tests disabled for Jakarta EE migration - require deployed application server
@Disabled("Tests require deployed Jakarta EE application server")
class JsfSmokeTest {
  @Test
  void mainXhtmlServed() {
    given().when().get("http://localhost:8080/dukeetf/main.xhtml")
      .then().statusCode(200)
      .header("Content-Type", startsWith("text/html"));
  }
}
