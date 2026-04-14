package org.woehlke.jakartaee.petclinic.specialty;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyDto;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyEndpointUtil;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyListDto;
import org.woehlke.jakartaee.petclinic.specialty.db.SpecialtyService;

import java.util.List;

/**
 *
 */
@Log
@Path("/specialty")
@ApplicationScoped
public class SpecialtyEndpoint {

    @Inject
    private SpecialtyService specialtyService;

    @Inject
    private SpecialtyEndpointUtil specialtyEndpointUtil;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SpecialtyDto> getList() {
        log.info("getList");
        return this.specialtyEndpointUtil.dtoListFactory(specialtyService.getAll()).getSpecialty();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public SpecialtyDto getEntity(@PathParam("id") Long id) {
        log.info("getEntity");
        return this.specialtyEndpointUtil.dtoFactory(specialtyService.findById(id));
    }

    @GET
    @Path("/list+json")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SpecialtyDto> getListAsJson() {
        log.info("getList");
        return this.specialtyEndpointUtil.dtoListFactory(specialtyService.getAll()).getSpecialty();
    }

    @GET
    @Path("/{id}+json")
    @Produces(MediaType.APPLICATION_JSON)
    public SpecialtyDto getEntityAsJson(@PathParam("id") Long id) {
        log.info("getEntity");
        return this.specialtyEndpointUtil.dtoFactory(specialtyService.findById(id));
    }

    @GET
    @Path("/list+xml")
    @Produces(MediaType.APPLICATION_XML)
    public SpecialtyListDto getListAsXml() {
        log.info("getListAsXml");
        return this.specialtyEndpointUtil.dtoListFactory(specialtyService.getAll());
    }

    @GET
    @Path("/{id}+xml")
    @Produces(MediaType.APPLICATION_XML)
    public SpecialtyDto getEntityAsXml(@PathParam("id") Long id) {
        log.info("getEntityAsXml");
        return this.specialtyEndpointUtil.dtoFactory(specialtyService.findById(id));
    }
}
