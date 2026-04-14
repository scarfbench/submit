package org.woehlke.jakartaee.petclinic.application.conf;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Simplified for Quarkus migration - JSF removed
 */
public class MessageProvider implements Serializable {

    private static final long serialVersionUID = 3363265300512735983L;

    public ResourceBundle getBundle() {
        // Stub - no longer using FacesContext
        return ResourceBundle.getBundle("messages", Locale.ENGLISH);
    }

}
