package org.woehlke.jakartaee.petclinic.pet;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pet.api.PetDto;
import org.woehlke.jakartaee.petclinic.pet.api.PetEndpointUtil;
import org.woehlke.jakartaee.petclinic.pet.api.PetListDto;
import org.woehlke.jakartaee.petclinic.pet.db.PetService;

import java.util.List;

@Log
@Path("/pet")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class PetEndpoint {

    @Inject
    PetService petService;

    @Inject
    PetEndpointUtil petEndpointUtil;

    @GET
    @Path("/list")
    public List<PetDto> getList() {
        log.info("getList");
        return petEndpointUtil.dtoListFactory(petService.getAll()).getPet();
    }

    @GET
    @Path("/{id}")
    public PetDto getEntity(@PathParam("id") Long id) {
        log.info("getEntity");
        return petEndpointUtil.dtoFactory(petService.findById(id));
    }

    @GET
    @Path("/list+json")
    public List<PetDto> getListAsJson() {
        log.info("getListAsJson");
        return petEndpointUtil.dtoListFactory(petService.getAll()).getPet();
    }

    @GET
    @Path("/{id}+json")
    public PetDto getEntityAsJson(@PathParam("id") Long id) {
        log.info("getEntityAsJson");
        return petEndpointUtil.dtoFactory(petService.findById(id));
    }

    @GET
    @Path("/list+xml")
    public PetListDto getListAsXml() {
        log.info("getListAsXml");
        return petEndpointUtil.dtoListFactory(petService.getAll());
    }

    @GET
    @Path("/{id}+xml")
    public PetDto getEntityAsXml(@PathParam("id") Long id) {
        log.info("getEntityAsXml");
        return petEndpointUtil.dtoFactory(petService.findById(id));
    }
}
