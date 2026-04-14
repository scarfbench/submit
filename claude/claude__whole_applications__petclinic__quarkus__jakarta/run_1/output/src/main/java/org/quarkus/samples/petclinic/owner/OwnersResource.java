package org.quarkus.samples.petclinic.owner;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/owners")
public class OwnersResource {

    @PersistenceContext
    EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Owner> listOwners() {
        return em.createQuery("SELECT o FROM Owner o", Owner.class).getResultList();
    }
}
