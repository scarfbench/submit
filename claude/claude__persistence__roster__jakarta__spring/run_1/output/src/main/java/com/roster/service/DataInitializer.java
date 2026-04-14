package com.roster.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.roster.entity.League;
import com.roster.entity.SummerLeague;
import com.roster.entity.WinterLeague;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger("roster.service.DataInitializer");

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
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
