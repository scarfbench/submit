package jakarta.tutorial.web.dukeetf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

/**
 * This test requires a running Jakarta EE application server.
 * It has been disabled for basic compilation testing.
 * To enable, deploy the application to a Jakarta EE server and use a REST client for testing.
 */
@Disabled("Requires running Jakarta EE server")
class JsfSmokeTest {
  @Test
  void mainXhtmlServed() {
    // This test would require integration testing setup with Jakarta EE server
    // Consider using Arquillian or similar for integration testing
  }
}