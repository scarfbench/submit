package org.woehlke.jakartaee.petclinic.specialty;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
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

@Log
@Path("/specialty")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class SpecialtyEndpoint {

    @Inject
    SpecialtyService specialtyService;

    @Inject
    SpecialtyEndpointUtil specialtyEndpointUtil;

    @GET
    @Path("/list")
    public List<SpecialtyDto> getList() {
        log.info("getList");
        return specialtyEndpointUtil.dtoListFactory(specialtyService.getAll()).getSpecialty();
    }

    @GET
    @Path("/{id}")
    public SpecialtyDto getEntity(@PathParam("id") Long id) {
        log.info("getEntity");
        return specialtyEndpointUtil.dtoFactory(specialtyService.findById(id));
    }

    @GET
    @Path("/list+json")
    public List<SpecialtyDto> getListAsJson() {
        log.info("getListAsJson");
        return specialtyEndpointUtil.dtoListFactory(specialtyService.getAll()).getSpecialty();
    }

    @GET
    @Path("/{id}+json")
    public SpecialtyDto getEntityAsJson(@PathParam("id") Long id) {
        log.info("getEntityAsJson");
        return specialtyEndpointUtil.dtoFactory(specialtyService.findById(id));
    }

    @GET
    @Path("/list+xml")
    public SpecialtyListDto getListAsXml() {
        log.info("getListAsXml");
        return specialtyEndpointUtil.dtoListFactory(specialtyService.getAll());
    }

    @GET
    @Path("/{id}+xml")
    public SpecialtyDto getEntityAsXml(@PathParam("id") Long id) {
        log.info("getEntityAsXml");
        return specialtyEndpointUtil.dtoFactory(specialtyService.findById(id));
    }
}
