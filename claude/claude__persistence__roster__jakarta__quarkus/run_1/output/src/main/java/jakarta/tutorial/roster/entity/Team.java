/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.roster.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "PERSISTENCE_ROSTER_TEAM")
public class Team implements Serializable {
    private static final long serialVersionUID = 4797864229333271809L;

    @Id
    private String id;
    private String name;
    private String city;

    @ManyToMany
    @JoinTable(
        name = "PERSISTENCE_ROSTER_TEAM_PLAYER",
        joinColumns = @JoinColumn(name = "TEAM_ID", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "PLAYER_ID", referencedColumnName = "ID")
    )
    private Collection<Player> players = new ArrayList<>();

    @ManyToOne
    private League league;

    public Team() {
    }

    public Team(String id, String name, String city) {
        this.id = id;
        this.name = name;
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Collection<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Collection<Player> players) {
        this.players = players;
    }

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public void addPlayer(Player player) {
        this.getPlayers().add(player);
    }

    public void dropPlayer(Player player) {
        this.getPlayers().remove(player);
    }
}
