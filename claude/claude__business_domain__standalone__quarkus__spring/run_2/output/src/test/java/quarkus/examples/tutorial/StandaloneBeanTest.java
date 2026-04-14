package quarkus.examples.tutorial;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.logging.Logger;

@SpringBootTest
class StandaloneBeanTest {
    private static final Logger logger = Logger.getLogger("standalone");

    @Autowired
    private StandaloneBean standaloneBean;

    @Test
    public void testReturnMessage() throws Exception {
        logger.info("Testing StandaloneBean.returnMessage()");
        String expResult = "Greetings!";
        String result = standaloneBean.returnMessage();
        assertEquals(expResult, result);
    }
}
