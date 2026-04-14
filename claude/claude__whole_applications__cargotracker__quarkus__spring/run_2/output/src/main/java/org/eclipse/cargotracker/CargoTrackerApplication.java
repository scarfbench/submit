package org.eclipse.cargotracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"org.eclipse.cargotracker", "org.eclipse.pathfinder"})
@EnableJms
@EnableScheduling
public class CargoTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CargoTrackerApplication.class, args);
    }
}
