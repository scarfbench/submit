package org.woehlke.jakartaee.petclinic.pet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pet.api.PetListDto;
import org.woehlke.jakartaee.petclinic.pet.db.PetService;
import org.woehlke.jakartaee.petclinic.pet.api.PetDto;
import org.woehlke.jakartaee.petclinic.pet.api.PetEndpointUtil;

import java.util.List;

@Log
@Path("/pet")
@ApplicationScoped
public class PetEndpoint {

    @Inject
    PetService petService;

    @Inject
    PetEndpointUtil petEndpointUtil;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PetDto> getList() {
        log.info("getList");
        List<Pet> petList = petService.getAll();
        PetListDto dto = this.petEndpointUtil.dtoListFactory(petList);
        return dto.getPet();
    }

    @GET
    @Path("/list+json")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PetDto> getListAsJson() {
        log.info("getList");
        List<Pet> petList = petService.getAll();
        PetListDto dto = this.petEndpointUtil.dtoListFactory(petList);
        return dto.getPet();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PetDto getEntity(@PathParam("id") Long id) {
        log.info("getEntity");
        return this.petEndpointUtil.dtoFactory(petService.findById(id));
    }

    @GET
    @Path("/{id}+json")
    @Produces(MediaType.APPLICATION_JSON)
    public PetDto getEntityAsJson(@PathParam("id") Long id) {
        log.info("getEntity");
        return this.petEndpointUtil.dtoFactory(petService.findById(id));
    }

    @GET
    @Path("/list+xml")
    @Produces(MediaType.APPLICATION_XML)
    public PetListDto getListAsXml() {
        log.info("getListAsXml");
        List<Pet> petList = petService.getAll();
        return this.petEndpointUtil.dtoListFactory(petList);
    }

    @GET
    @Path("/{id}+xml")
    @Produces(MediaType.APPLICATION_XML)
    public PetDto getEntityAsXml(@PathParam("id") Long id) {
        log.info("getEntityAsXml");
        return this.petEndpointUtil.dtoFactory(petService.findById(id));
    }
}
