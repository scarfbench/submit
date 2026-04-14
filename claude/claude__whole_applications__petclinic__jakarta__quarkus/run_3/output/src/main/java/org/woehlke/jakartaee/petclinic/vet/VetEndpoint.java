package org.woehlke.jakartaee.petclinic.vet;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.vet.api.VetDto;
import org.woehlke.jakartaee.petclinic.vet.api.VetEndpointUtil;
import org.woehlke.jakartaee.petclinic.vet.api.VetListDto;
import org.woehlke.jakartaee.petclinic.vet.db.VetService;

import java.util.List;

@Log
@Path("/vet")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class VetEndpoint {

    @Inject
    VetService vetService;

    @Inject
    VetEndpointUtil vetEndpointUtil;

    @GET
    @Path("/list")
    public List<VetDto> getList() {
        log.info("getList");
        return vetEndpointUtil.dtoListFactory(vetService.getAll()).getVet();
    }

    @GET
    @Path("/{id}")
    public VetDto getEntity(@PathParam("id") Long id) {
        log.info("getEntity");
        return vetEndpointUtil.dtoFactory(vetService.findById(id));
    }

    @GET
    @Path("/list+json")
    public List<VetDto> getListAsJson() {
        log.info("getListAsJson");
        return vetEndpointUtil.dtoListFactory(vetService.getAll()).getVet();
    }

    @GET
    @Path("/{id}+json")
    public VetDto getEntityAsJson(@PathParam("id") Long id) {
        log.info("getEntityAsJson");
        return vetEndpointUtil.dtoFactory(vetService.findById(id));
    }

    @GET
    @Path("/list+xml")
    public VetListDto getListAsXml() {
        log.info("getListAsXml");
        return vetEndpointUtil.dtoListFactory(vetService.getAll());
    }

    @GET
    @Path("/{id}+xml")
    public VetDto getEntityAsXml(@PathParam("id") Long id) {
        log.info("getEntityAsXml");
        return vetEndpointUtil.dtoFactory(vetService.findById(id));
    }
}
