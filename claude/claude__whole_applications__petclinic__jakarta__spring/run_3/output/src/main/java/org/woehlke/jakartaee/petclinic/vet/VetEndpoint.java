package org.woehlke.jakartaee.petclinic.vet;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.vet.api.VetDto;
import org.woehlke.jakartaee.petclinic.vet.api.VetEndpointUtil;
import org.woehlke.jakartaee.petclinic.vet.db.VetService;

import java.util.List;

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
    public List<VetDto> getList() {
        log.info("getList");
        return this.vetEndpointUtil.dtoListFactory(vetService.getAll()).getVetList();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public VetDto getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        return this.vetEndpointUtil.dtoFactory(vetService.findById(id));
    }
}
