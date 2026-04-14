package org.woehlke.jakartaee.petclinic.visit;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.visit.api.VisitDto;
import org.woehlke.jakartaee.petclinic.visit.api.VisitEndpointUtil;
import org.woehlke.jakartaee.petclinic.visit.api.VisitListDto;
import org.woehlke.jakartaee.petclinic.visit.db.VisitService;

import java.util.List;

@Log
@Path("/visit")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class VisitEndpoint {

    @Inject
    VisitService visitService;

    @Inject
    VisitEndpointUtil visitEndpointUtil;

    @GET
    @Path("/list")
    public List<VisitDto> getList() {
        log.info("getList");
        return visitEndpointUtil.dtoListFactory(visitService.getAll()).getVisit();
    }

    @GET
    @Path("/{id}")
    public VisitDto getEntity(@PathParam("id") Long id) {
        log.info("getEntity");
        return visitEndpointUtil.dtoFactory(visitService.findById(id));
    }

    @GET
    @Path("/list+json")
    public List<VisitDto> getListAsJson() {
        log.info("getListAsJson");
        return visitEndpointUtil.dtoListFactory(visitService.getAll()).getVisit();
    }

    @GET
    @Path("/{id}+json")
    public VisitDto getEntityAsJson(@PathParam("id") Long id) {
        log.info("getEntityAsJson");
        return visitEndpointUtil.dtoFactory(visitService.findById(id));
    }

    @GET
    @Path("/list+xml")
    public VisitListDto getListAsXml() {
        log.info("getListAsXml");
        return visitEndpointUtil.dtoListFactory(visitService.getAll());
    }

    @GET
    @Path("/{id}+xml")
    public VisitDto getEntityAsXml(@PathParam("id") Long id) {
        log.info("getEntityAsXml");
        return visitEndpointUtil.dtoFactory(visitService.findById(id));
    }
}
