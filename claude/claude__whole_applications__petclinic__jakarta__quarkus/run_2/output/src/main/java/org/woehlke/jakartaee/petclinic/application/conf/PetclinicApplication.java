package org.woehlke.jakartaee.petclinic.application.conf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.java.Log;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Quarkus Application configuration bean.
 * Note: The REST path is configured via quarkus.rest.path in application.properties.
 * Does not extend jakarta.ws.rs.core.Application because Quarkus RESTEasy Reactive
 * does not allow @Inject in Application subclasses.
 */
@Log
@ApplicationScoped
public class PetclinicApplication {

    @Inject
    @MessageBundle
    private ResourceBundle msg;

    public ResourceBundle getMsg() {
        if (msg == null) {
            // Fallback if injection fails
            return ResourceBundle.getBundle("messages", Locale.ENGLISH);
        }
        return msg;
    }
}
