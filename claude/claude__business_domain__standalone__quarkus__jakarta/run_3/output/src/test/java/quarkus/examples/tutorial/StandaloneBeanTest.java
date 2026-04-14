package quarkus.examples.tutorial;

import jakarta.inject.Inject;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.logging.Logger;

@ExtendWith(WeldJunit5Extension.class)
class StandaloneBeanTest {
    private static final Logger logger = Logger.getLogger("standalone");

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(StandaloneBean.class).build();

    @Inject
    private StandaloneBean standaloneBean;

    @Test
    public void testReturnMessage() throws Exception {
        logger.info("Testing StandaloneBean.returnMessage()");
        String expResult = "Greetings!";
        String result = standaloneBean.returnMessage();
        assertEquals(expResult, result);
    }
}
