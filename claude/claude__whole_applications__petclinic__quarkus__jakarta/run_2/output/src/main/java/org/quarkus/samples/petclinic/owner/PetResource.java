package org.quarkus.samples.petclinic.owner;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.quarkus.samples.petclinic.system.TemplateEngine;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/owners")
public class PetResource {

    @Inject
    TemplateEngine templateEngine;

    @Inject
    Validator validator;

    @PersistenceContext
    EntityManager em;

    @GET
    @Path("{ownerId}/pets/new")
    @Produces(MediaType.TEXT_HTML)
    public String createTemplate(@PathParam("ownerId") Long ownerId) {
        Owner owner = em.find(Owner.class, ownerId);
        List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
        Map<String, Object> vars = new HashMap<>();
        vars.put("owner", owner);
        vars.put("pet", null);
        vars.put("petTypes", petTypes);
        vars.put("errors", new HashMap<>());
        return templateEngine.render("createOrUpdatePetForm", vars);
    }

    @GET
    @Path("{ownerId}/pets/{petId}/edit")
    @Produces(MediaType.TEXT_HTML)
    public String editTemplate(@PathParam("ownerId") Long ownerId, @PathParam("petId") Long petId) {
        Owner owner = em.find(Owner.class, ownerId);
        Pet pet = em.find(Pet.class, petId);
        List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
        Map<String, Object> vars = new HashMap<>();
        vars.put("owner", owner);
        vars.put("pet", pet);
        vars.put("petTypes", petTypes);
        vars.put("errors", new HashMap<>());
        return templateEngine.render("createOrUpdatePetForm", vars);
    }

    @POST
    @Path("{ownerId}/pets/new")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public String processCreationForm(@PathParam("ownerId") Long ownerId,
                                       @FormParam("name") String name,
                                       @FormParam("birthDate") String birthDateStr,
                                       @FormParam("type") String type) {
        Owner owner = em.find(Owner.class, ownerId);

        Pet pet = new Pet();
        pet.name = name;
        pet.birthDate = parseDateSafe(birthDateStr);
        pet.type = parsePetType(type);

        final Set<ConstraintViolation<Pet>> violations = validator.validate(pet);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Pet> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", owner);
            vars.put("pet", null);
            vars.put("petTypes", petTypes);
            vars.put("errors", errors);
            return templateEngine.render("createOrUpdatePetForm", vars);
        } else {
            pet.owner = owner;
            em.persist(pet);
            owner.addPet(pet);
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", owner);
            return templateEngine.render("ownerDetails", vars);
        }
    }

    @POST
    @Path("{ownerId}/pets/{petId}/edit")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public String processUpdateForm(@PathParam("ownerId") Long ownerId,
                                     @PathParam("petId") Long petId,
                                     @FormParam("name") String name,
                                     @FormParam("birthDate") String birthDateStr,
                                     @FormParam("type") String type) {
        Pet pet = em.find(Pet.class, petId);
        pet.name = name;
        pet.birthDate = parseDateSafe(birthDateStr);
        pet.type = parsePetType(type);

        final Set<ConstraintViolation<Pet>> violations = validator.validate(pet);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Pet> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            List<PetType> petTypes = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", pet.owner);
            vars.put("pet", pet);
            vars.put("petTypes", petTypes);
            vars.put("errors", errors);
            return templateEngine.render("createOrUpdatePetForm", vars);
        } else {
            Pet merged = em.merge(pet);
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", merged.owner);
            return templateEngine.render("ownerDetails", vars);
        }
    }

    private PetType parsePetType(String text) {
        List<PetType> types = em.createQuery("SELECT pt FROM PetType pt", PetType.class).getResultList();
        for (PetType pt : types) {
            if (pt.name.equals(text)) {
                return pt;
            }
        }
        throw new IllegalArgumentException("type not found: " + text);
    }

    private LocalDate parseDateSafe(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return LocalDate.parse(dateStr.trim());
    }
}
