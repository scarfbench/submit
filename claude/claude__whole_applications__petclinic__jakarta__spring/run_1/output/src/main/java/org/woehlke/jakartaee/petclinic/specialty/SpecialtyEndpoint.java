package org.woehlke.jakartaee.petclinic.specialty;


import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyDto;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyEndpointUtil;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyListDto;
import org.woehlke.jakartaee.petclinic.specialty.db.SpecialtyService;

import java.util.List;

/**
 *
 */
@Log
@RestController
@RequestMapping("/rest/specialty")
public class SpecialtyEndpoint {

    private final SpecialtyService specialtyService;
    private final SpecialtyEndpointUtil specialtyEndpointUtil;

    @Autowired
    public SpecialtyEndpoint(SpecialtyService specialtyService, SpecialtyEndpointUtil specialtyEndpointUtil) {
        this.specialtyService = specialtyService;
        this.specialtyEndpointUtil = specialtyEndpointUtil;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SpecialtyDto>> getList() {
        log.info("getList");
        List<SpecialtyDto> specialties = this.specialtyEndpointUtil.dtoListFactory(specialtyService.getAll()).getSpecialty();
        return ResponseEntity.ok(specialties);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SpecialtyDto> getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        Specialty specialty = specialtyService.findById(id);
        if (specialty == null) {
            return ResponseEntity.notFound().build();
        }
        SpecialtyDto dto = this.specialtyEndpointUtil.dtoFactory(specialty);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/list+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SpecialtyDto>> getListAsJson() {
        log.info("getList");
        List<SpecialtyDto> specialties = this.specialtyEndpointUtil.dtoListFactory(specialtyService.getAll()).getSpecialty();
        return ResponseEntity.ok(specialties);
    }

    @GetMapping(value = "/{id}+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SpecialtyDto> getEntityAsJson(@PathVariable("id") Long id) {
        log.info("getEntity");
        Specialty specialty = specialtyService.findById(id);
        if (specialty == null) {
            return ResponseEntity.notFound().build();
        }
        SpecialtyDto dto = this.specialtyEndpointUtil.dtoFactory(specialty);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/list+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<SpecialtyListDto> getListAsXml() {
        log.info("getListAsXml");
        SpecialtyListDto dto = this.specialtyEndpointUtil.dtoListFactory(specialtyService.getAll());
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/{id}+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<SpecialtyDto> getEntityAsXml(@PathVariable("id") Long id) {
        log.info("getEntityAsXml");
        Specialty specialty = specialtyService.findById(id);
        if (specialty == null) {
            return ResponseEntity.notFound().build();
        }
        SpecialtyDto dto = this.specialtyEndpointUtil.dtoFactory(specialty);
        return ResponseEntity.ok(dto);
    }
}
