// src/test/java/spring/tutorial/mood/web/MoodControllerTest.java
package spring.tutorial.mood.web;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class MoodControllerTest {

    @Test
    void reportLoads() {
        given()
            .queryParam("name", "Duke")
            .when()
            .get("/report")
            .then()
            .statusCode(200)
            .body(containsString("current mood"))
            .body(containsString("/images/duke.waving.gif"));
    }
}
