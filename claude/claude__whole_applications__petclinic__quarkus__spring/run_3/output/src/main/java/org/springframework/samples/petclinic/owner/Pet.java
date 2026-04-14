package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.samples.petclinic.visit.Visit;

@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    @NotEmpty
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private PetType type;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonBackReference
    private Owner owner;

    @Transient
    private Set<Visit> visits = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public PetType getType() {
        return type;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Set<Visit> getVisits() {
        if (this.visits == null) {
            this.visits = new HashSet<>();
        }
        return this.visits;
    }

    public void setVisits(Set<Visit> visits) {
        this.visits = new LinkedHashSet<>(visits);
    }

    public List<Visit> getSortedVisits() {
        List<Visit> sortedVisits = new ArrayList<>(getVisits());
        sortedVisits.sort((v1, v2) -> {
            if (v1.getDate() == null || v2.getDate() == null) return 0;
            return v1.getDate().compareTo(v2.getDate());
        });
        return Collections.unmodifiableList(sortedVisits);
    }

    public void addVisit(Visit visit) {
        getVisits().add(visit);
        visit.setPetId(this.id);
    }

    public boolean isNew() {
        return this.id == null;
    }

    @Override
    public String toString() {
        return "Pet [birthDate=" + birthDate + ", name=" + name + ", type=" + type + "]";
    }
}
