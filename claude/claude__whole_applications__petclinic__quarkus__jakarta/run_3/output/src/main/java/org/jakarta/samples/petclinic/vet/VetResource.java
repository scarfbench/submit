package org.jakarta.samples.petclinic.vet;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/vets")
public class VetResource {

    @Inject
    EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Vets showResourcesVetList() {
        Vets vets = new Vets();
        List<Vet> vetList = em.createQuery("SELECT v FROM Vet v", Vet.class).getResultList();
        vets.getVetList().addAll(vetList);
        return vets;
    }
}
