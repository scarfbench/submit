package org.woehlke.jakartaee.petclinic.vet.db;

import jakarta.persistence.*;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.vet.Vet;

@Log
public class VetListener {

    @PrePersist
    public void onPrePersist(Object domainObject) {
        if (domainObject instanceof Vet) { log.info("VetListener.onPrePersist: " + domainObject.toString()); }
    }

    @PreUpdate
    public void onPreUpdate(Object domainObject) {
        if (domainObject instanceof Vet) { log.info("VetListener.onPreUpdate: " + domainObject.toString()); }
    }

    @PreRemove
    public void onPreRemove(Object domainObject) {
        if (domainObject instanceof Vet) { log.info("VetListener.onPreRemove: " + domainObject.toString()); }
    }
}
