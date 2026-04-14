package org.jakarta.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
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
import org.jakarta.samples.petclinic.visit.Visit;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "name")
    @NotEmpty
    public String name;

    @Column(name = "birth_date")
    public LocalDate birthDate;

    @ManyToOne
    @JoinColumn(name = "type_id")
    public PetType type;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonBackReference
    public Owner owner;

    @Transient
    public Set<Visit> visits = new LinkedHashSet<>();

    // Getters and setters for JSP EL
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public PetType getType() { return type; }
    public void setType(PetType type) { this.type = type; }
    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) { this.owner = owner; }
    public Set<Visit> getVisits() { return getVisitsInternal(); }

    protected Set<Visit> getVisitsInternal() {
        if (this.visits == null) {
            this.visits = new HashSet<>();
        }
        return this.visits;
    }

    public void setVisitsInternal(Collection<Visit> visits) {
        this.visits = new LinkedHashSet<>(visits);
    }

    public List<Visit> getSortedVisits() {
        List<Visit> sortedVisits = new ArrayList<>(getVisitsInternal());
        Collections.sort(sortedVisits, new VisitComparator());
        return Collections.unmodifiableList(sortedVisits);
    }

    public void addVisit(Visit visit) {
        getVisitsInternal().add(visit);
        visit.petId = this.id;
    }

    public boolean isNew() {
        return this.id == null;
    }

    public String getFormattedBirthDate() {
        return birthDate != null ? birthDate.toString() : "";
    }

    @Override
    public String toString() {
        return "Pet [birthDate=" + birthDate + ", name=" + name + ", type=" + type + "]";
    }
}
