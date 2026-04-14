package org.woehlke.jakartaee.petclinic.visit;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.visit.api.VisitDto;
import org.woehlke.jakartaee.petclinic.visit.api.VisitListDto;
import org.woehlke.jakartaee.petclinic.visit.db.VisitService;
import org.woehlke.jakartaee.petclinic.visit.api.VisitEndpointUtil;

import java.util.List;

/**
 *
 */
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
    public ResponseEntity<List<VisitDto>> getList() {
        log.info("getList");
        List<VisitDto> visits = this.visitEndpointUtil.dtoListFactory(visitService.getAll()).getVisit();
        return ResponseEntity.ok(visits);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VisitDto> getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        Visit visit = visitService.findById(id);
        if (visit == null) {
            return ResponseEntity.notFound().build();
        }
        VisitDto dto = this.visitEndpointUtil.dtoFactory(visit);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/list+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VisitDto>> getListAsJson() {
        log.info("getList");
        List<VisitDto> visits = this.visitEndpointUtil.dtoListFactory(visitService.getAll()).getVisit();
        return ResponseEntity.ok(visits);
    }

    @GetMapping(value = "/{id}+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VisitDto> getEntityAsJson(@PathVariable("id") Long id) {
        log.info("getEntity");
        Visit visit = visitService.findById(id);
        if (visit == null) {
            return ResponseEntity.notFound().build();
        }
        VisitDto dto = this.visitEndpointUtil.dtoFactory(visit);
        return ResponseEntity.ok(dto);
    }


    @GetMapping(value = "/list+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<VisitListDto> getListAsXml() {
        log.info("getListAsXml");
        VisitListDto dto = this.visitEndpointUtil.dtoListFactory(visitService.getAll());
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/{id}+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<VisitDto> getEntityAsXml(@PathVariable("id") Long id) {
        log.info("getEntityAsXml");
        Visit visit = visitService.findById(id);
        if (visit == null) {
            return ResponseEntity.notFound().build();
        }
        VisitDto dto = this.visitEndpointUtil.dtoFactory(visit);
        return ResponseEntity.ok(dto);
    }

}
