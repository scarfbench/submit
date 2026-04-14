package org.quarkus.samples.petclinic.vet;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/vets")
public class VetResource {

    @PersistenceContext
    EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Vets showResourcesVetList() {
        Vets vets = new Vets();
        vets.getVetList().addAll(em.createQuery("SELECT v FROM Vet v", Vet.class).getResultList());
        return vets;
    }
}
