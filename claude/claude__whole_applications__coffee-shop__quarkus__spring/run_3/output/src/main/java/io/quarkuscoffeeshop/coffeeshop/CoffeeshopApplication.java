package io.quarkuscoffeeshop.coffeeshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"io.quarkuscoffeeshop"})
@EnableAsync
public class CoffeeshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoffeeshopApplication.class, args);
    }
}
