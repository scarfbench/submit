package org.woehlke.jakartaee.petclinic.visit;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.visit.api.VisitDto;
import org.woehlke.jakartaee.petclinic.visit.api.VisitEndpointUtil;
import org.woehlke.jakartaee.petclinic.visit.db.VisitService;

import java.util.List;

@Log
@RestController
@RequestMapping("/rest/visit")
public class VisitEndpoint {

    private final VisitService visitService;
    private final VisitEndpointUtil visitEndpointUtil;

    @Autowired
    public VisitEndpoint(VisitService visitService, VisitEndpointUtil visitEndpointUtil) {
        this.visitService = visitService;
        this.visitEndpointUtil = visitEndpointUtil;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<VisitDto> getList() {
        log.info("getList");
        return this.visitEndpointUtil.dtoListFactory(visitService.getAll()).getVisit();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VisitDto getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        return this.visitEndpointUtil.dtoFactory(visitService.findById(id));
    }
}
