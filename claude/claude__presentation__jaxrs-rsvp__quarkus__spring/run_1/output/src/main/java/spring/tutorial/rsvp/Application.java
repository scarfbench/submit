package spring.tutorial.rsvp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import jakarta.faces.webapp.FacesServlet;

/**
 * Spring Boot Application Entry Point
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Register JSF FacesServlet with Spring Boot
     */
    @Bean
    public ServletRegistrationBean<FacesServlet> facesServletRegistration() {
        ServletRegistrationBean<FacesServlet> registration = new ServletRegistrationBean<>(
            new FacesServlet(), "*.xhtml");
        registration.setLoadOnStartup(1);
        return registration;
    }
}
