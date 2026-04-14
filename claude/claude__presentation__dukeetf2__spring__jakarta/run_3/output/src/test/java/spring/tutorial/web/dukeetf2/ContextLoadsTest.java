package spring.tutorial.web.dukeetf2;

import org.junit.jupiter.api.Test;

class ContextLoadsTest {
    @Test
    void contextLoads() {
        // Basic test to ensure classes can be loaded
        ETFEndpoint endpoint = new ETFEndpoint();
        PriceVolumeBean bean = new PriceVolumeBean();
        assert endpoint != null;
        assert bean != null;
    }
}
