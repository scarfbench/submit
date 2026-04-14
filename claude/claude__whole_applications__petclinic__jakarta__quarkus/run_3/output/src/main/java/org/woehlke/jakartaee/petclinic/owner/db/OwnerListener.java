package org.woehlke.jakartaee.petclinic.owner.db;

import jakarta.persistence.*;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.owner.Owner;

@Log
public class OwnerListener {

    @PrePersist
    public void onPrePersist(Object domainObject) {
        if (domainObject instanceof Owner) {
            log.info("OwnerListener.onPrePersist: " + domainObject.toString());
        }
    }

    @PreUpdate
    public void onPreUpdate(Object domainObject) {
        if (domainObject instanceof Owner) {
            log.info("OwnerListener.onPreUpdate: " + domainObject.toString());
        }
    }

    @PreRemove
    public void onPreRemove(Object domainObject) {
        if (domainObject instanceof Owner) {
            log.info("OwnerListener.onPreRemove: " + domainObject.toString());
        }
    }
}
