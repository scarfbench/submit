package com.roster.entity;

import java.io.Serializable;

import com.roster.util.IncorrectSportException;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("WINTER")
public class WinterLeague extends League implements Serializable {
    private static final long serialVersionUID = 8942582153559426625L;

    public WinterLeague() {
    }

    public WinterLeague(String id, String name, String sport) throws
            IncorrectSportException {
        this.setId(id);
        this.setName(name);
        if (sport.equalsIgnoreCase("hockey") ||
                sport.equalsIgnoreCase("skiing") ||
                sport.equalsIgnoreCase("snowboarding")) {
            this.setSport(sport);
        } else {
            throw new IncorrectSportException("Sport is not a winter sport.");
        }
    }
}
