package spring.examples.tutorial.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.extension.ExtendWith;
import spring.examples.tutorial.standalone.service.StandaloneService;

import jakarta.inject.Inject;

@ExtendWith(WeldJunit5Extension.class)
class StandaloneApplicationTests {

	private static final Logger logger = Logger.getLogger("standalone.service");

	@WeldSetup
	public WeldInitiator weld = WeldInitiator.from(StandaloneService.class).build();

	@Inject
	private StandaloneService standaloneService;

	@Test
	void contextLoads() {
		assertNotNull(standaloneService, "StandaloneService should be injected");
	}

	@Test
	public void testReturnMessage() throws Exception {
		logger.info("Testing standalone.service.StandaloneService.returnMessage()");
		String expResult = "Greetings!";
		String result = standaloneService.returnMessage();
		assertEquals(expResult, result);
	}

}
