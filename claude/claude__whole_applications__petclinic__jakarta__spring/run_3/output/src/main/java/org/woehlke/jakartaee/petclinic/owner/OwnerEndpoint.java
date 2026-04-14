package org.woehlke.jakartaee.petclinic.owner;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerDto;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerEndpointUtil;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerListDto;
import org.woehlke.jakartaee.petclinic.owner.db.OwnerService;

import java.util.List;

@Log
@RestController
@RequestMapping("/rest/owner")
public class OwnerEndpoint {

    private final OwnerService ownerService;
    private final OwnerEndpointUtil ownerEndpointUtil;

    @Autowired
    public OwnerEndpoint(OwnerService ownerService, OwnerEndpointUtil ownerEndpointUtil) {
        this.ownerService = ownerService;
        this.ownerEndpointUtil = ownerEndpointUtil;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OwnerDto> getList() {
        log.info("getList");
        return ownerEndpointUtil.dtoListFactory(ownerService.getAll()).getOwner();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OwnerDto getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        Owner owner = ownerService.findById(id);
        return ownerEndpointUtil.dtoFactory(owner);
    }
}
