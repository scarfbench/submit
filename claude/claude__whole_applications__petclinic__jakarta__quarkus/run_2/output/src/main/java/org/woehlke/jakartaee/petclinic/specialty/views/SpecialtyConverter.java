package org.woehlke.jakartaee.petclinic.specialty.views;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import java.io.Serializable;


/**
 * Simplified for Quarkus migration - JSF Converter removed
 */
@Log
@ApplicationScoped
public class SpecialtyConverter implements Serializable {

    private static final long serialVersionUID = 3816519727799645701L;

    public Specialty getAsObject(String name) {
        Specialty specialty = new Specialty(name);
        log.info("SpecialtyConverter.getAsObject: from = " + name + " to " + specialty.toString());
        return specialty;
    }

    public String getAsString(Specialty specialty) {
        String name = specialty.getName();
        log.info("SpecialtyConverter.getAsString: from " + specialty.toString() + " to " + name);
        return name;
    }

    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: "+SpecialtyConverter.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: "+SpecialtyConverter.class.getSimpleName());
    }

}
