package org.woehlke.jakartaee.petclinic.pet;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.pet.api.PetDto;
import org.woehlke.jakartaee.petclinic.pet.api.PetEndpointUtil;
import org.woehlke.jakartaee.petclinic.pet.api.PetListDto;
import org.woehlke.jakartaee.petclinic.pet.db.PetService;

import java.util.List;

@Log
@RestController
@RequestMapping("/rest/pet")
public class PetEndpoint {

    private final PetService petService;
    private final PetEndpointUtil petEndpointUtil;

    @Autowired
    public PetEndpoint(PetService petService, PetEndpointUtil petEndpointUtil) {
        this.petService = petService;
        this.petEndpointUtil = petEndpointUtil;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PetDto> getList() {
        log.info("getList");
        List<Pet> petList = petService.getAll();
        PetListDto dto = this.petEndpointUtil.dtoListFactory(petList);
        return dto.getPet();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PetDto getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        return this.petEndpointUtil.dtoFactory(petService.findById(id));
    }
}
