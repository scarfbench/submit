package jakarta.tutorial.roster.request;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakartaee.tutorial.roster.entity.League;
import jakartaee.tutorial.roster.entity.SummerLeague;
import jakartaee.tutorial.roster.entity.WinterLeague;

@ApplicationScoped
public class DataInitializer {

    private static final Logger logger = Logger.getLogger("roster.request.DataInitializer");

    @Inject
    EntityManager em;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        seedCanonicalLeagues();
    }

    private void seedCanonicalLeagues() {
        try {
            if (em.find(League.class, "L1") == null) {
                em.persist(new SummerLeague("L1", "Mountain", "Soccer"));
            }
            if (em.find(League.class, "L2") == null) {
                em.persist(new SummerLeague("L2", "Valley", "Basketball"));
            }
            if (em.find(League.class, "L3") == null) {
                em.persist(new SummerLeague("L3", "Foothills", "Soccer"));
            }
            if (em.find(League.class, "L4") == null) {
                em.persist(new WinterLeague("L4", "Alpine", "Snowboarding"));
            }
            logger.info("Canonical leagues seeded successfully");
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to seed canonical leagues", ex);
        }
    }
}
