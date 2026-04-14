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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.quarkus.samples.petclinic.system.TemplateEngine;
import org.quarkus.samples.petclinic.visit.Visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/owners")
public class OwnersResource {

    @Inject
    TemplateEngine templateEngine;

    @Inject
    Validator validator;

    @PersistenceContext
    EntityManager em;

    @GET
    @Path("/find")
    @Produces(MediaType.TEXT_HTML)
    public String findTemplate() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("errors", Collections.emptyList());
        return templateEngine.render("findOwners", vars);
    }

    @GET
    @Path("new")
    @Produces(MediaType.TEXT_HTML)
    public String createTemplate() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("owner", null);
        vars.put("errors", new HashMap<>());
        return templateEngine.render("createOrUpdateOwnerForm", vars);
    }

    @GET
    @Path("{ownerId}/edit")
    @Produces(MediaType.TEXT_HTML)
    public String editTemplate(@PathParam("ownerId") Long ownerId) {
        Owner owner = em.find(Owner.class, ownerId);
        Map<String, Object> vars = new HashMap<>();
        vars.put("owner", owner);
        vars.put("errors", new HashMap<>());
        return templateEngine.render("createOrUpdateOwnerForm", vars);
    }

    @GET
    @Path("{ownerId}")
    @Produces(MediaType.TEXT_HTML)
    public String showOwner(@PathParam("ownerId") Long ownerId) {
        Owner owner = em.find(Owner.class, ownerId);
        if (owner != null) {
            setVisits(owner);
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("owner", owner);
        return templateEngine.render("ownerDetails", vars);
    }

    @POST
    @Path("new")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public String processCreationForm(
            @FormParam("firstName") String firstName,
            @FormParam("lastName") String lastName,
            @FormParam("address") String address,
            @FormParam("city") String city,
            @FormParam("telephone") String telephone) {

        Owner owner = new Owner();
        owner.firstName = firstName;
        owner.lastName = lastName;
        owner.address = address;
        owner.city = city;
        owner.telephone = telephone;

        final Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Owner> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", null);
            vars.put("errors", errors);
            return templateEngine.render("createOrUpdateOwnerForm", vars);
        } else {
            em.persist(owner);
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", owner);
            return templateEngine.render("ownerDetails", vars);
        }
    }

    @POST
    @Path("{ownerId}/edit")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String processUpdateOwnerForm(
            @FormParam("firstName") String firstName,
            @FormParam("lastName") String lastName,
            @FormParam("address") String address,
            @FormParam("city") String city,
            @FormParam("telephone") String telephone,
            @PathParam("ownerId") Long ownerId) {

        Owner owner = new Owner();
        owner.id = ownerId;
        owner.firstName = firstName;
        owner.lastName = lastName;
        owner.address = address;
        owner.city = city;
        owner.telephone = telephone;

        final Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
        final Map<String, String> errors = new HashMap<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<Owner> violation : violations) {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", owner);
            vars.put("errors", errors);
            return templateEngine.render("createOrUpdateOwnerForm", vars);
        } else {
            Owner merged = em.merge(owner);
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", merged);
            return templateEngine.render("ownerDetails", vars);
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String processFindForm(@QueryParam("lastName") String lastName) {

        List<Owner> owners;

        if (lastName == null || "".equals(lastName.trim())) {
            owners = em.createQuery("SELECT o FROM Owner o", Owner.class).getResultList();
        } else {
            owners = em.createQuery("SELECT o FROM Owner o WHERE o.lastName = :lastName", Owner.class)
                    .setParameter("lastName", lastName)
                    .getResultList();
        }

        if (owners.isEmpty()) {
            Map<String, Object> vars = new HashMap<>();
            List<String> errorList = new ArrayList<>();
            errorList.add("lastName not found");
            vars.put("errors", errorList);
            return templateEngine.render("findOwners", vars);
        }
        if (owners.size() == 1) {
            Owner owner = owners.get(0);
            setVisits(owner);
            Map<String, Object> vars = new HashMap<>();
            vars.put("owner", owner);
            return templateEngine.render("ownerDetails", vars);
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("owners", owners);
        return templateEngine.render("ownersList", vars);
    }

    protected void setVisits(Owner owner) {
        if (owner.pets != null) {
            for (Pet pet : owner.pets) {
                List<Visit> visits = em.createQuery("SELECT v FROM Visit v WHERE v.petId = :petId", Visit.class)
                        .setParameter("petId", pet.id)
                        .getResultList();
                pet.setVisitsInternal(visits);
            }
        }
    }

    @Path("/api/list")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Owner> listOwners() {
        return em.createQuery("SELECT o FROM Owner o", Owner.class).getResultList();
    }
}
