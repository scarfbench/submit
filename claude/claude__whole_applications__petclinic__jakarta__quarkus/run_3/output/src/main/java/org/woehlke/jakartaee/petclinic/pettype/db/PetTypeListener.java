package org.woehlke.jakartaee.petclinic.pettype.db;

import jakarta.persistence.*;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

@Log
public class PetTypeListener {

    @PrePersist
    public void onPrePersist(Object domainObject) {
        if (domainObject instanceof PetType) { log.info("PetTypeListener.onPrePersist: " + domainObject.toString()); }
    }

    @PreUpdate
    public void onPreUpdate(Object domainObject) {
        if (domainObject instanceof PetType) { log.info("PetTypeListener.onPreUpdate: " + domainObject.toString()); }
    }

    @PreRemove
    public void onPreRemove(Object domainObject) {
        if (domainObject instanceof PetType) { log.info("PetTypeListener.onPreRemove: " + domainObject.toString()); }
    }
}
