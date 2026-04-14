package org.woehlke.jakartaee.petclinic.specialty.db;

import jakarta.persistence.*;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

@Log
public class SpecialtyListener {

    @PrePersist
    public void onPrePersist(Object domainObject) {
        if (domainObject instanceof Specialty) { log.info("SpecialtyListener.onPrePersist: " + domainObject.toString()); }
    }

    @PreUpdate
    public void onPreUpdate(Object domainObject) {
        if (domainObject instanceof Specialty) { log.info("SpecialtyListener.onPreUpdate: " + domainObject.toString()); }
    }

    @PreRemove
    public void onPreRemove(Object domainObject) {
        if (domainObject instanceof Specialty) { log.info("SpecialtyListener.onPreRemove: " + domainObject.toString()); }
    }
}
