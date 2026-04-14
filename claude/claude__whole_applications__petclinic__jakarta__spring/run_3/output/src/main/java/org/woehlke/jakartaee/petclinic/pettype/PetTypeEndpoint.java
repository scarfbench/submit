package org.woehlke.jakartaee.petclinic.pettype;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeDto;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeEndpointUtil;
import org.woehlke.jakartaee.petclinic.pettype.db.PetTypeService;

import java.util.List;

@Log
@RestController
@RequestMapping("/rest/petType")
public class PetTypeEndpoint {

    private final PetTypeService petTypeService;
    private final PetTypeEndpointUtil petTypeEndpointUtil;

    @Autowired
    public PetTypeEndpoint(PetTypeService petTypeService, PetTypeEndpointUtil petTypeEndpointUtil) {
        this.petTypeService = petTypeService;
        this.petTypeEndpointUtil = petTypeEndpointUtil;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PetTypeDto> getList() {
        log.info("getList");
        return this.petTypeEndpointUtil.dtoListFactory(petTypeService.getAll()).getPetType();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PetTypeDto getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        return this.petTypeEndpointUtil.dtoFactory(petTypeService.findById(id));
    }
}
