package com.coffeeshop.kitchen.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class BasicHealth implements HealthIndicator {
    @Override
    public Health health() {
        return Health.up().withDetail("service", "kitchen-service").build();
    }
}
