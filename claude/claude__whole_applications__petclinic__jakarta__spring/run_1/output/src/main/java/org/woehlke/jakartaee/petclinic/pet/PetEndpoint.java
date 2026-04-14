package org.woehlke.jakartaee.petclinic.pet;


import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.pet.api.PetListDto;
import org.woehlke.jakartaee.petclinic.pet.db.PetService;
import org.woehlke.jakartaee.petclinic.pet.api.PetDto;
import org.woehlke.jakartaee.petclinic.pet.api.PetEndpointUtil;

import java.util.List;

/**
 *
 */
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
    public ResponseEntity<List<PetDto>> getList() {
        log.info("------------------------------------------------------------");
        log.info("getList");
        log.info("------------------------------------------------------------");
        List<Pet> petList = petService.getAll();
        PetListDto dto = this.petEndpointUtil.dtoListFactory(petList);
        log.info(dto.toString());
        log.info("------------------------------------------------------------");
        return ResponseEntity.ok(dto.getPet());
    }

    @GetMapping(value = "/list+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PetDto>> getListAsJson() {
        log.info("------------------------------------------------------------");
        log.info("getList");
        log.info("------------------------------------------------------------");
        List<Pet> petList = petService.getAll();
        PetListDto dto = this.petEndpointUtil.dtoListFactory(petList);
        log.info(dto.toString());
        log.info("------------------------------------------------------------");
        return ResponseEntity.ok(dto.getPet());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PetDto> getEntity(@PathVariable("id") Long id) {
        log.info("------------------------------------------------------------");
        log.info("getEntity");
        log.info("------------------------------------------------------------");
        Pet pet = petService.findById(id);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }
        PetDto dto = this.petEndpointUtil.dtoFactory(pet);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/{id}+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PetDto> getEntityAsJson(@PathVariable("id") Long id) {
        log.info("------------------------------------------------------------");
        log.info("getEntity");
        log.info("------------------------------------------------------------");
        Pet pet = petService.findById(id);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }
        PetDto oPetDto = this.petEndpointUtil.dtoFactory(pet);
        log.info("------------------------------------------------------------");
        log.info(oPetDto.toString());
        log.info("------------------------------------------------------------");
        return ResponseEntity.ok(oPetDto);
    }

    @GetMapping(value = "/list+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<PetListDto> getListAsXml() {
        log.info("------------------------------------------------------------");
        log.info("getListAsXml");
        log.info("------------------------------------------------------------");
        List<Pet> petList = petService.getAll();
        log.info("------------------------------------------------------------");
        PetListDto dto = this.petEndpointUtil.dtoListFactory(petList);
        log.info("------------------------------------------------------------");
        log.info(dto.toString());
        log.info("------------------------------------------------------------");
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/{id}+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<PetDto> getEntityAsXml(@PathVariable("id") Long id) {
        log.info("------------------------------------------------------------");
        log.info("getEntityAsXml");
        log.info("------------------------------------------------------------");
        Pet pet = petService.findById(id);
        if (pet == null) {
            return ResponseEntity.notFound().build();
        }
        PetDto oPetDto = this.petEndpointUtil.dtoFactory(pet);
        log.info("------------------------------------------------------------");
        log.info(oPetDto.toString());
        log.info("------------------------------------------------------------");
        return ResponseEntity.ok(oPetDto);
    }

}
