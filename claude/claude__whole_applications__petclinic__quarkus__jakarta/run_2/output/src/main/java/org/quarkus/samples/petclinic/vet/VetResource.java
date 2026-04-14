package org.quarkus.samples.petclinic.vet;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.quarkus.samples.petclinic.system.TemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/")
public class VetResource {

    @Inject
    TemplateEngine templateEngine;

    @PersistenceContext
    EntityManager em;

    @GET
    @Path("/vets.html")
    @Produces(MediaType.TEXT_HTML)
    public String showResourcesVetPage() {
        List<Vet> vets = em.createQuery("SELECT v FROM Vet v", Vet.class).getResultList();
        Map<String, Object> vars = new HashMap<>();
        vars.put("vets", vets);
        return templateEngine.render("vetList", vars);
    }

    @GET
    @Path("/vets")
    @Produces(MediaType.APPLICATION_JSON)
    public Vets showResourcesVetList() {
        Vets vets = new Vets();
        vets.getVetList().addAll(em.createQuery("SELECT v FROM Vet v", Vet.class).getResultList());
        return vets;
    }
}
