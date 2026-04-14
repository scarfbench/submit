package org.jakarta.samples.petclinic.system;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.jakarta.samples.petclinic.owner.Owner;
import org.jakarta.samples.petclinic.owner.Pet;
import org.jakarta.samples.petclinic.owner.PetType;
import org.jakarta.samples.petclinic.vet.Specialty;
import org.jakarta.samples.petclinic.vet.Vet;
import org.jakarta.samples.petclinic.visit.Visit;

import java.time.LocalDate;

/**
 * Initializes seed data on application startup.
 * This replaces the Quarkus/Hibernate import.sql approach.
 */
@Singleton
@Startup
public class DataInitializer {

    @PersistenceContext(unitName = "petclinic")
    EntityManager em;

    @PostConstruct
    @Transactional
    public void init() {
        // Check if data already exists
        Long count = em.createQuery("SELECT COUNT(v) FROM Vet v", Long.class).getSingleResult();
        if (count > 0) {
            return;
        }

        // Vets
        Vet vet1 = createVet(1001L, "James", "Carter");
        Vet vet2 = createVet(1002L, "Helen", "Leary");
        Vet vet3 = createVet(1003L, "Linda", "Douglas");
        Vet vet4 = createVet(1004L, "Rafael", "Ortega");
        Vet vet5 = createVet(1005L, "Henry", "Stevens");
        Vet vet6 = createVet(1006L, "Sharon", "Jenkins");

        // Specialties
        Specialty radiology = createSpecialty(1001L, "radiology");
        Specialty surgery = createSpecialty(1002L, "surgery");
        Specialty dentistry = createSpecialty(1003L, "dentistry");

        // Vet specialties
        vet2.addSpecialty(radiology);
        vet3.addSpecialty(surgery);
        vet3.addSpecialty(dentistry);
        vet4.addSpecialty(surgery);
        vet5.addSpecialty(radiology);

        // Pet types
        PetType cat = createPetType(1001L, "cat");
        PetType dog = createPetType(1002L, "dog");
        PetType lizard = createPetType(1003L, "lizard");
        PetType snake = createPetType(1004L, "snake");
        PetType bird = createPetType(1005L, "bird");
        PetType hamster = createPetType(1006L, "hamster");

        // Owners
        Owner o1 = createOwner(1001L, "George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");
        Owner o2 = createOwner(1002L, "Betty", "Davis", "638 Cardinal Ave.", "Sun Prairie", "6085551749");
        Owner o3 = createOwner(1003L, "Eduardo", "Rodriquez", "2693 Commerce St.", "McFarland", "6085558763");
        Owner o4 = createOwner(1004L, "Harold", "Davis", "563 Friendly St.", "Windsor", "6085553198");
        Owner o5 = createOwner(1005L, "Peter", "McTavish", "2387 S. Fair Way", "Madison", "6085552765");
        Owner o6 = createOwner(1006L, "Jean", "Coleman", "105 N. Lake St.", "Monona", "6085552654");
        Owner o7 = createOwner(1007L, "Jeff", "Black", "1450 Oak Blvd.", "Monona", "6085555387");
        Owner o8 = createOwner(1008L, "Maria", "Escobito", "345 Maple St.", "Madison", "6085557683");
        Owner o9 = createOwner(1009L, "David", "Schroeder", "2749 Blackhawk Trail", "Madison", "6085559435");
        Owner o10 = createOwner(1010L, "Carlos", "Estaban", "2335 Independence La.", "Waunakee", "6085555487");

        // Pets
        createPet(1001L, "Leo", LocalDate.of(2010, 9, 7), cat, o1);
        createPet(1002L, "Basil", LocalDate.of(2012, 8, 6), hamster, o2);
        createPet(1003L, "Rosy", LocalDate.of(2011, 4, 17), dog, o3);
        createPet(1004L, "Jewel", LocalDate.of(2010, 3, 7), dog, o3);
        createPet(1005L, "Iggy", LocalDate.of(2010, 11, 30), lizard, o4);
        createPet(1006L, "George", LocalDate.of(2010, 1, 20), snake, o5);
        Pet samantha = createPet(1007L, "Samantha", LocalDate.of(2012, 9, 4), cat, o6);
        Pet max = createPet(1008L, "Max", LocalDate.of(2012, 9, 4), cat, o6);
        createPet(1009L, "Lucky", LocalDate.of(2011, 8, 6), bird, o7);
        createPet(1010L, "Mulligan", LocalDate.of(2007, 2, 24), dog, o8);
        createPet(1011L, "Freddy", LocalDate.of(2010, 3, 9), bird, o9);
        createPet(1012L, "Lucky", LocalDate.of(2010, 6, 24), dog, o10);
        createPet(1013L, "Sly", LocalDate.of(2012, 6, 8), cat, o10);

        // Visits
        createVisit(1001L, 1007L, LocalDate.of(2013, 1, 1), "rabies shot");
        createVisit(1002L, 1008L, LocalDate.of(2013, 1, 2), "rabies shot");
        createVisit(1003L, 1008L, LocalDate.of(2013, 1, 3), "neutered");
        createVisit(1004L, 1007L, LocalDate.of(2013, 1, 4), "spayed");

        em.flush();
    }

    private Vet createVet(Long id, String firstName, String lastName) {
        Vet vet = new Vet();
        vet.firstName = firstName;
        vet.lastName = lastName;
        em.persist(vet);
        return vet;
    }

    private Specialty createSpecialty(Long id, String name) {
        Specialty s = new Specialty();
        s.name = name;
        em.persist(s);
        return s;
    }

    private PetType createPetType(Long id, String name) {
        PetType pt = new PetType();
        pt.name = name;
        em.persist(pt);
        return pt;
    }

    private Owner createOwner(Long id, String firstName, String lastName, String address, String city, String telephone) {
        Owner o = new Owner();
        o.firstName = firstName;
        o.lastName = lastName;
        o.address = address;
        o.city = city;
        o.telephone = telephone;
        em.persist(o);
        return o;
    }

    private Pet createPet(Long id, String name, LocalDate birthDate, PetType type, Owner owner) {
        Pet pet = new Pet();
        pet.name = name;
        pet.birthDate = birthDate;
        pet.type = type;
        pet.owner = owner;
        em.persist(pet);
        owner.addPet(pet);
        return pet;
    }

    private Visit createVisit(Long id, Long petId, LocalDate date, String description) {
        Visit v = new Visit();
        v.date = date;
        v.description = description;
        v.petId = petId;
        em.persist(v);
        return v;
    }
}
