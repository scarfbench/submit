package org.woehlke.jakartaee.petclinic.pettype;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeDto;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeEndpointUtil;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeListDto;
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
    public ResponseEntity<List<PetTypeDto>> getList() {
        log.info("getList");
        List<PetTypeDto> petTypes = this.petTypeEndpointUtil.dtoListFactory(petTypeService.getAll()).getPetType();
        return ResponseEntity.ok(petTypes);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PetTypeDto> getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        PetType petType = petTypeService.findById(id);
        if (petType == null) {
            return ResponseEntity.notFound().build();
        }
        PetTypeDto dto = this.petTypeEndpointUtil.dtoFactory(petType);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/list+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PetTypeDto>> getListAsJson() {
        log.info("getList");
        List<PetTypeDto> petTypes = this.petTypeEndpointUtil.dtoListFactory(petTypeService.getAll()).getPetType();
        return ResponseEntity.ok(petTypes);
    }

    @GetMapping(value = "/{id}+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PetTypeDto> getEntityAsJson(@PathVariable("id") Long id) {
        log.info("getEntity");
        PetType petType = petTypeService.findById(id);
        if (petType == null) {
            return ResponseEntity.notFound().build();
        }
        PetTypeDto dto = this.petTypeEndpointUtil.dtoFactory(petType);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/list+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<PetTypeListDto> getListAsXml() {
        log.info("getListAsXml");
        PetTypeListDto dto = this.petTypeEndpointUtil.dtoListFactory(petTypeService.getAll());
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/{id}+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<PetTypeDto> getEntityAsXml(@PathVariable("id") Long id) {
        log.info("getEntityAsXml");
        PetType petType = petTypeService.findById(id);
        if (petType == null) {
            return ResponseEntity.notFound().build();
        }
        PetTypeDto dto = this.petTypeEndpointUtil.dtoFactory(petType);
        return ResponseEntity.ok(dto);
    }

}
