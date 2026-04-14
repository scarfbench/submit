package org.woehlke.jakartaee.petclinic.application.views;

import jakarta.inject.Inject;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.application.conf.PetclinicApplication;
import org.woehlke.jakartaee.petclinic.application.framework.EntityBase;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Stub implementation for Quarkus migration - JSF removed
 */
@Log
@Named("flashMessagesView")
@ApplicationScoped
public class FlashMessagesViewImpl implements FlashMessagesView, Serializable {

    private static final long serialVersionUID = -2267751568724878682L;

    @Inject
    private PetclinicApplication petclinicApplication;

    public void flashTheMessages(){
        // Stub - no longer using FacesContext
        log.fine("flashTheMessages called (stub)");
    }

    public void addInfoMessage(String summary, String detail) {
        log.info("INFO: " + summary + " - " + detail);
    }

    public void addWarnMessage(String summary, String detail) {
        log.warning("WARN: " + summary + " - " + detail);
    }

    @Override
    public void addInfoMessage(String summary, EntityBase entity) {
        log.info("INFO: " + summary + " - " + (entity != null ? entity.getPrimaryKey() : "null"));
    }

    @Override
    public void addWarnMessage(String summary, EntityBase entity) {
        log.warning("WARN: " + summary + " - " + (entity != null ? entity.getPrimaryKey() : "null"));
    }

    @Override
    public void addErrorMessage(String summary, EntityBase entity) {
        log.severe("ERROR: " + summary + " - " + (entity != null ? entity.getPrimaryKey() : "null"));
    }

    @Override
    public void addWarnMessage(RuntimeException e, EntityBase entity) {
        log.warning("WARN: " + e.getLocalizedMessage() + " - " + (entity != null ? entity.getPrimaryKey() : "null"));
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: "+ FlashMessagesViewImpl.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: "+ FlashMessagesViewImpl.class.getSimpleName());
    }

}
