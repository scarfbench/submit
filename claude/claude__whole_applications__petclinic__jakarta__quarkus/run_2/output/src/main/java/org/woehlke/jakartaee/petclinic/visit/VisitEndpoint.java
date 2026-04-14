package org.woehlke.jakartaee.petclinic.visit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.visit.api.VisitDto;
import org.woehlke.jakartaee.petclinic.visit.api.VisitListDto;
import org.woehlke.jakartaee.petclinic.visit.db.VisitService;
import org.woehlke.jakartaee.petclinic.visit.api.VisitEndpointUtil;

import java.util.List;

@Log
@Path("/visit")
@ApplicationScoped
public class VisitEndpoint {

    @Inject
    VisitService visitService;

    @Inject
    VisitEndpointUtil visitEndpointUtil;

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<VisitDto> getList() {
        log.info("getList");
        return this.visitEndpointUtil.dtoListFactory(visitService.getAll()).getVisit();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public VisitDto getEntity(@PathParam("id") Long id) {
        log.info("getEntity");
        return this.visitEndpointUtil.dtoFactory(visitService.findById(id));
    }

    @GET
    @Path("/list+json")
    @Produces(MediaType.APPLICATION_JSON)
    public List<VisitDto> getListAsJson() {
        log.info("getList");
        return this.visitEndpointUtil.dtoListFactory(visitService.getAll()).getVisit();
    }

    @GET
    @Path("/{id}+json")
    @Produces(MediaType.APPLICATION_JSON)
    public VisitDto getEntityAsJson(@PathParam("id") Long id) {
        log.info("getEntity");
        return this.visitEndpointUtil.dtoFactory(visitService.findById(id));
    }

    @GET
    @Path("/list+xml")
    @Produces(MediaType.APPLICATION_XML)
    public VisitListDto getListAsXml() {
        log.info("getListAsXml");
        return this.visitEndpointUtil.dtoListFactory(visitService.getAll());
    }

    @GET
    @Path("/{id}+xml")
    @Produces(MediaType.APPLICATION_XML)
    public VisitDto getEntityAsXml(@PathParam("id") Long id) {
        log.info("getEntityAsXml");
        return this.visitEndpointUtil.dtoFactory(visitService.findById(id));
    }
}
