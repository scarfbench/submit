package com.ibm.websphere.samples.daytrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class DayTraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DayTraderApplication.class, args);
    }
}
