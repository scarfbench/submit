package org.woehlke.jakartaee.petclinic.visit.db;

import jakarta.persistence.*;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.visit.Visit;

@Log
public class VisitListener {

    @PrePersist
    public void onPrePersist(Object domainObject) {
        if (domainObject instanceof Visit) { log.info("VisitListener.onPrePersist: " + domainObject.toString()); }
    }

    @PreUpdate
    public void onPreUpdate(Object domainObject) {
        if (domainObject instanceof Visit) { log.info("VisitListener.onPreUpdate: " + domainObject.toString()); }
    }

    @PreRemove
    public void onPreRemove(Object domainObject) {
        if (domainObject instanceof Visit) { log.info("VisitListener.onPreRemove: " + domainObject.toString()); }
    }
}
