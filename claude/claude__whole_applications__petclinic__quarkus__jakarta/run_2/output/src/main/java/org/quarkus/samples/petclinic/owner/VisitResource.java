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
import org.quarkus.samples.petclinic.visit.Visit;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Path("/owners")
public class VisitResource {

    @Inject
    TemplateEngine templateEngine;

    @Inject
    Validator validator;

    @PersistenceContext
    EntityManager em;

    @GET
    @Path("{ownerId}/pets/{petId}/visits/new")
    @Produces(MediaType.TEXT_HTML)
    public String createTemplate(@PathParam("petId") Long petId) {
        Pet pet = em.find(Pet.class, petId);
        Map<String, Object> vars = new HashMap<>();
        vars.put("pet", pet);
        vars.put("visit", null);
        vars.put("errors", new HashMap<>());
        return templateEngine.render("createOrUpdateVisitForm", vars);
    }

    @POST
    @Path("{ownerId}/pets/{petId}/visits/new")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public String processCreationForm(@PathParam("petId") Long petId,
                                       @FormParam("date") String dateStr,
                                       @FormParam("description") String description) {

        Pet pet = em.find(Pet.class, petId);
        Visit visit = new Visit();
        visit.date = parseDateSafe(dateStr);
        visit.description = description;
        visit.petId = petId;

        final Set<ConstraintViolation<Visit>> violations = validator.validate(visit);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Visit> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            Map<String, Object> vars = new HashMap<>();
            vars.put("pet", pet);
            vars.put("visit", visit);
            vars.put("errors", errors);
            return templateEngine.render("createOrUpdateVisitForm", vars);
        } else {
            em.persist(visit);
            pet.addVisit(visit);
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", pet.owner);
            return templateEngine.render("ownerDetails", vars);
        }
    }

    private LocalDate parseDateSafe(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return LocalDate.parse(dateStr.trim());
    }
}
