package com.coffeeshop.barista.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.coffeeshop.barista")
public class BaristaApplication {
    public static void main(String[] args) {
        SpringApplication.run(BaristaApplication.class, args);
    }
}
