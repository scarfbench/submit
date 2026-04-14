package com.roster.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "PERSISTENCE_ROSTER_PLAYER")
public class Player implements Serializable {
    private static final long serialVersionUID = -2760127516426049966L;

    @Id
    private String id;
    private String name;
    private String position;
    private double salary;

    @ManyToMany(mappedBy = "players")
    private Collection<Team> teams = new ArrayList<>();

    public Player() {
    }

    public Player(String id, String name, String position, double salary) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.salary = salary;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public Collection<Team> getTeams() {
        return teams;
    }

    public void setTeams(Collection<Team> teams) {
        this.teams = teams;
    }

    public void addTeam(Team team) {
        this.getTeams().add(team);
    }

    public void dropTeam(Team team) {
        this.getTeams().remove(team);
    }
}
