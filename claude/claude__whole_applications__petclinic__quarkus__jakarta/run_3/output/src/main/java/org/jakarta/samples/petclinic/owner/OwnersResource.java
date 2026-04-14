package org.jakarta.samples.petclinic.owner;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/owners")
public class OwnersResource {

    @Inject
    EntityManager em;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Owner> listOwners() {
        return em.createQuery("SELECT o FROM Owner o", Owner.class).getResultList();
    }
}
