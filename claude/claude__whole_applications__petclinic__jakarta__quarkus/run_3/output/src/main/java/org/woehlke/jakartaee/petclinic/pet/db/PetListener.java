package org.woehlke.jakartaee.petclinic.pet.db;

import jakarta.persistence.*;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pet.Pet;

@Log
public class PetListener {

    @PrePersist
    public void onPrePersist(Object domainObject) {
        if (domainObject instanceof Pet) { log.info("PetListener.onPrePersist: " + domainObject.toString()); }
    }

    @PreUpdate
    public void onPreUpdate(Object domainObject) {
        if (domainObject instanceof Pet) { log.info("PetListener.onPreUpdate: " + domainObject.toString()); }
    }

    @PreRemove
    public void onPreRemove(Object domainObject) {
        if (domainObject instanceof Pet) { log.info("PetListener.onPreRemove: " + domainObject.toString()); }
    }
}
