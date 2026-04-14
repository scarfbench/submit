package org.woehlke.jakartaee.petclinic.pettype.views;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

import java.io.Serializable;

/**
 * Simplified for Quarkus migration - JSF Converter removed
 */
@Log
@ApplicationScoped
public class PetTypeConverter implements Serializable {

    private static final long serialVersionUID = 4908876595996046904L;

    public PetType getAsObject(String name) {
        PetType petType = new PetType(name);
        log.info("PetTypeConverter.getAsObject: from = " + name + " to " + petType.toString());
        return petType;
    }

    public String getAsString(PetType petType) {
        String name = petType.getName();
        log.info("PetTypeConverter.getAsObject: from = " + petType.toString() + " to " + name);
        return name;
    }


    @PostConstruct
    public void postConstruct() {
        log.info("postConstruct: "+PetTypeConverter.class.getSimpleName());
    }

    @PreDestroy
    public void preDestroy() {
        log.info("preDestroy: "+PetTypeConverter.class.getSimpleName());
    }

}
