package org.woehlke.jakartaee.petclinic.application.conf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "org.woehlke.jakartaee.petclinic")
@EntityScan(basePackages = "org.woehlke.jakartaee.petclinic")
@EnableJpaRepositories(basePackages = "org.woehlke.jakartaee.petclinic")
public class PetclinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetclinicApplication.class, args);
    }
}
