package org.woehlke.jakartaee.petclinic.specialty;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyDto;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyEndpointUtil;
import org.woehlke.jakartaee.petclinic.specialty.db.SpecialtyService;

import java.util.List;

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
    public List<SpecialtyDto> getList() {
        log.info("getList");
        return this.specialtyEndpointUtil.dtoListFactory(specialtyService.getAll()).getSpecialty();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SpecialtyDto getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        return this.specialtyEndpointUtil.dtoFactory(specialtyService.findById(id));
    }
}
