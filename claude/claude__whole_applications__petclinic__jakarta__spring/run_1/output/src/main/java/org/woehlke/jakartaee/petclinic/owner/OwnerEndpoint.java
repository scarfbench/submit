package org.woehlke.jakartaee.petclinic.owner;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerDto;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerEndpointUtil;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerListDto;
import org.woehlke.jakartaee.petclinic.owner.db.OwnerService;

import java.util.List;

/**
 *
 */
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
    public ResponseEntity<List<OwnerDto>> getList() {
        log.info("getList");
        List<OwnerDto> owners = ownerEndpointUtil.dtoListFactory(ownerService.getAll()).getOwner();
        return ResponseEntity.ok(owners);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OwnerDto> getEntity(@PathVariable("id") Long id) {
        log.info("getEntity");
        Owner owner = ownerService.findById(id);
        if (owner == null) {
            return ResponseEntity.notFound().build();
        }
        OwnerDto dto = ownerEndpointUtil.dtoFactory(owner);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/list+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OwnerDto>> getListAsJson() {
        log.info("getList");
        List<OwnerDto> owners = ownerEndpointUtil.dtoListFactory(ownerService.getAll()).getOwner();
        return ResponseEntity.ok(owners);
    }

    @GetMapping(value = "/{id}+json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OwnerDto> getEntityAsJson(@PathVariable("id") Long id) {
        log.info("getEntity");
        Owner owner = ownerService.findById(id);
        if (owner == null) {
            return ResponseEntity.notFound().build();
        }
        OwnerDto dto = ownerEndpointUtil.dtoFactory(owner);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/list+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<OwnerListDto> getListAsXml() {
        log.info("getListAsXml");
        OwnerListDto dto = ownerEndpointUtil.dtoListFactory(ownerService.getAll());
        return ResponseEntity.ok(dto);
    }

    @GetMapping(value = "/{id}+xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<OwnerDto> getEntityAsXml(@PathVariable("id") Long id) {
        log.info("getEntityAsXml");
        Owner owner = ownerService.findById(id);
        if (owner == null) {
            return ResponseEntity.notFound().build();
        }
        OwnerDto dto = ownerEndpointUtil.dtoFactory(owner);
        return ResponseEntity.ok(dto);
    }

}
