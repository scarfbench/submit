package org.woehlke.jakartaee.petclinic.owner;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerDto;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerEndpointUtil;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerListDto;
import org.woehlke.jakartaee.petclinic.owner.db.OwnerService;

import java.util.List;

@Log
@Path("/owner")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class OwnerEndpoint {

    @Inject
    OwnerService ownerService;

    @Inject
    OwnerEndpointUtil ownerEndpointUtil;

    @GET
    @Path("/list")
    public List<OwnerDto> getList() {
        log.info("getList");
        return ownerEndpointUtil.dtoListFactory(ownerService.getAll()).getOwner();
    }

    @GET
    @Path("/{id}")
    public OwnerDto getEntity(@PathParam("id") Long id) {
        log.info("getEntity");
        return ownerEndpointUtil.dtoFactory(ownerService.findById(id));
    }

    @GET
    @Path("/list+json")
    public List<OwnerDto> getListAsJson() {
        log.info("getListAsJson");
        return ownerEndpointUtil.dtoListFactory(ownerService.getAll()).getOwner();
    }

    @GET
    @Path("/{id}+json")
    public OwnerDto getEntityAsJson(@PathParam("id") Long id) {
        log.info("getEntityAsJson");
        return ownerEndpointUtil.dtoFactory(ownerService.findById(id));
    }

    @GET
    @Path("/list+xml")
    public OwnerListDto getListAsXml() {
        log.info("getListAsXml");
        return ownerEndpointUtil.dtoListFactory(ownerService.getAll());
    }

    @GET
    @Path("/{id}+xml")
    public OwnerDto getEntityAsXml(@PathParam("id") Long id) {
        log.info("getEntityAsXml");
        return ownerEndpointUtil.dtoFactory(ownerService.findById(id));
    }
}
