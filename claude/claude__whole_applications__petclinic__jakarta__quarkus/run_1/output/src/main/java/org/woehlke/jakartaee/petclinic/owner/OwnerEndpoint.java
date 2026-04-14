package org.woehlke.jakartaee.petclinic.owner;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerDto;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerListDto;
import org.woehlke.jakartaee.petclinic.owner.db.OwnerService;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerEndpointUtil;

import java.util.List;

/**
 *
 */
@Log
@Path("/owner")
@ApplicationScoped
public class OwnerEndpoint {

    @Inject
    private OwnerService ownerService;

    @Inject
    private OwnerEndpointUtil ownerEndpointUtil;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OwnerDto> getList() {
        log.info("getList");
        return ownerEndpointUtil.dtoListFactory(ownerService.getAll()).getOwner();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public OwnerDto getEntity(@PathParam("id") Long id) {
        log.info("getEntity");
        Owner owner = ownerService.findById(id);
        OwnerDto dto = ownerEndpointUtil.dtoFactory(owner);
        return dto;
    }

    @GET
    @Path("/list+json")
    @Produces(MediaType.APPLICATION_JSON)
    public List<OwnerDto> getListAsJson() {
        log.info("getList");
        return ownerEndpointUtil.dtoListFactory(ownerService.getAll()).getOwner();
    }

    @GET
    @Path("/{id}+json")
    @Produces(MediaType.APPLICATION_JSON)
    public OwnerDto getEntityAsJson(@PathParam("id") Long id) {
        log.info("getEntity");
        return ownerEndpointUtil.dtoFactory(ownerService.findById(id));
    }

    @GET
    @Path("/list+xml")
    @Produces(MediaType.APPLICATION_XML)
    public OwnerListDto getListAsXml() {
        log.info("getListAsXml");
        return ownerEndpointUtil.dtoListFactory(ownerService.getAll());
    }

    @GET
    @Path("/{id}+xml")
    @Produces(MediaType.APPLICATION_XML)
    public OwnerDto getEntityAsXml(@PathParam("id") Long id) {
        log.info("getEntityAsXml");
        return ownerEndpointUtil.dtoFactory(ownerService.findById(id));
    }

}
