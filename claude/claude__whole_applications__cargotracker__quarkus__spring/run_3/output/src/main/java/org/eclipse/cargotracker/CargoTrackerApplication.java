package org.eclipse.cargotracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"org.eclipse.cargotracker", "org.eclipse.pathfinder"})
@EnableScheduling
public class CargoTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CargoTrackerApplication.class, args);
    }
}
