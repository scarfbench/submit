package org.woehlke.jakartaee.petclinic.application.conf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * CDI Producer for ResourceBundle messages
 */
@ApplicationScoped
public class MessageBundleProducer {

    @Produces
    @MessageBundle
    public ResourceBundle produceMessageBundle() {
        return ResourceBundle.getBundle("messages", Locale.ENGLISH);
    }
}
