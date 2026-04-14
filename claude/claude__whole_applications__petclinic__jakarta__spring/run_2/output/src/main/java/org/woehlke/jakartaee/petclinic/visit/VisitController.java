package org.woehlke.jakartaee.petclinic.visit;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.visit.api.VisitDto;
import org.woehlke.jakartaee.petclinic.visit.api.VisitEndpointUtil;
import org.woehlke.jakartaee.petclinic.visit.db.Visit;
import org.woehlke.jakartaee.petclinic.visit.db.VisitService;

import java.util.List;

@Log
@RestController
@RequestMapping("/rest/visit")
public class VisitController {

    private final VisitService visitService;
    private final VisitEndpointUtil visitEndpointUtil;

    @Autowired
    public VisitController(VisitService visitService, VisitEndpointUtil visitEndpointUtil) {
        this.visitService = visitService;
        this.visitEndpointUtil = visitEndpointUtil;
    }

    @GetMapping("/list")
    public ResponseEntity<List<VisitDto>> getAll() {
        List<Visit> visits = visitService.getAll();
        List<VisitDto> dtos = visitEndpointUtil.convertToDtoList(visits);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitDto> getById(@PathVariable Long id) {
        Visit visit = visitService.findById(id);
        if (visit == null) {
            return ResponseEntity.notFound().build();
        }
        VisitDto dto = visitEndpointUtil.convertToDto(visit);
        return ResponseEntity.ok(dto);
    }
}
