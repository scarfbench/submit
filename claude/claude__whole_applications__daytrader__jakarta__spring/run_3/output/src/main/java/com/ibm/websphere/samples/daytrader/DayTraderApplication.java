package com.ibm.websphere.samples.daytrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
@EnableScheduling
public class DayTraderApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(DayTraderApplication.class, args);
    }
}
