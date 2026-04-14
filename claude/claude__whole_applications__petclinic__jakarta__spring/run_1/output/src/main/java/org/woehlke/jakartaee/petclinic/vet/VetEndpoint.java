package org.woehlke.jakartaee.petclinic.vet;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.vet.api.VetDto;
import org.woehlke.jakartaee.petclinic.vet.api.VetListDto;
import org.woehlke.jakartaee.petclinic.vet.db.VetService;
import org.woehlke.jakartaee.petclinic.vet.api.VetEndpointUtil;


import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tw
 * Date: 05.01.14
 * Time: 09:27
 * To change this template use File | Settings | File Templates.
 */
@Log
@RestController
@RequestMapping("/rest/vet")
public class VetEndpoint {

    private final VetService vetService;
    private final VetEndpointUtil vetEndpointUtil;

    @Autowired
    public VetEndpoint(VetService vetService, VetEndpointUtil vetEndpointUtil) {
        this.vetService = vetService;
        this.vetEndpointUtil = vetEndpointUtil;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VetDto>> getList() {
        log.info("getList");
        List<VetDto> vets = this.vetEndpointUtil.dtoListFactory(vetService.getAll()).getVetList();
        return ResponseEntity.ok(vets);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VetDto> getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        Vet vet = vetService.findById(id);
        if (vet == null) {
            return ResponseEntity.notFound().build();
        }
        VetDto dto = this.vetEndpointUtil.dtoFactory(vet);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/list+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VetDto>> getListAsJson() {
        log.info("getList");
        List<VetDto> vets = this.vetEndpointUtil.dtoListFactory(vetService.getAll()).getVetList();
        return ResponseEntity.ok(vets);
    }

    @GetMapping(value = "/{id}+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VetDto> getEntityAsJson(@PathVariable("id") Long id) {
        log.info("getEntity");
        Vet vet = vetService.findById(id);
        if (vet == null) {
            return ResponseEntity.notFound().build();
        }
        VetDto dto = this.vetEndpointUtil.dtoFactory(vet);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/list+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<VetListDto> getListAsXml() {
        log.info("getListAsXml");
        VetListDto dto = this.vetEndpointUtil.dtoListFactory(vetService.getAll());
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/{id}+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<VetDto> getEntityAsXml(@PathVariable("id") Long id) {
        log.info("getEntityAsXml");
        Vet vet = vetService.findById(id);
        if (vet == null) {
            return ResponseEntity.notFound().build();
        }
        VetDto dto = this.vetEndpointUtil.dtoFactory(vet);
        return ResponseEntity.ok(dto);
    }

}
