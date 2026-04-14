package org.woehlke.jakartaee.petclinic.pettype;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeDto;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeEndpointUtil;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeListDto;
import org.woehlke.jakartaee.petclinic.pettype.db.PetTypeService;

import java.util.List;

@Log
@Path("/petType")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class PetTypeEndpoint {

    @Inject
    PetTypeService petTypeService;

    @Inject
    PetTypeEndpointUtil petTypeEndpointUtil;

    @GET
    @Path("/list")
    public List<PetTypeDto> getList() {
        log.info("getList");
        return petTypeEndpointUtil.dtoListFactory(petTypeService.getAll()).getPetType();
    }

    @GET
    @Path("/{id}")
    public PetTypeDto getEntity(@PathParam("id") Long id) {
        log.info("getEntity");
        return petTypeEndpointUtil.dtoFactory(petTypeService.findById(id));
    }

    @GET
    @Path("/list+json")
    public List<PetTypeDto> getListAsJson() {
        log.info("getListAsJson");
        return petTypeEndpointUtil.dtoListFactory(petTypeService.getAll()).getPetType();
    }

    @GET
    @Path("/{id}+json")
    public PetTypeDto getEntityAsJson(@PathParam("id") Long id) {
        log.info("getEntityAsJson");
        return petTypeEndpointUtil.dtoFactory(petTypeService.findById(id));
    }

    @GET
    @Path("/list+xml")
    public PetTypeListDto getListAsXml() {
        log.info("getListAsXml");
        return petTypeEndpointUtil.dtoListFactory(petTypeService.getAll());
    }

    @GET
    @Path("/{id}+xml")
    public PetTypeDto getEntityAsXml(@PathParam("id") Long id) {
        log.info("getEntityAsXml");
        return petTypeEndpointUtil.dtoFactory(petTypeService.findById(id));
    }
}
