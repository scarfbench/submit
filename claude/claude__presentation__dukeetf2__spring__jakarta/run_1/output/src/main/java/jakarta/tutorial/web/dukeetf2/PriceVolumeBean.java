package jakarta.tutorial.web.dukeetf2;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named("priceVolumeBean")
@ApplicationScoped
public class PriceVolumeBean {
    private final Random random = new Random();
    private volatile double price = 100.0;
    private volatile int volume = 300000;
    private static final Logger logger = Logger.getLogger("PriceVolumeBean");

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, "Initializing service.");
        // Schedule the periodic task
        if (DukeEtfApplication.getScheduler() != null) {
            DukeEtfApplication.getScheduler().scheduleWithFixedDelay(
                this::timeout, 1000, 1000, TimeUnit.MILLISECONDS
            );
        }
    }

    @PreDestroy
    public void destroy() {
        logger.log(Level.INFO, "Destroying service.");
    }

    public void timeout() {
        price += 1.0 * (random.nextInt(100) - 50) / 100.0;
        volume += random.nextInt(5000) - 2500;
        ETFEndpoint.send(price, volume);
    }
}