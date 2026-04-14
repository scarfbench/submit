package org.woehlke.jakartaee.petclinic.owner;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerDto;
import org.woehlke.jakartaee.petclinic.owner.api.OwnerEndpointUtil;
import org.woehlke.jakartaee.petclinic.owner.db.Owner;
import org.woehlke.jakartaee.petclinic.owner.db.OwnerService;

import java.util.List;

@Log
@RestController
@RequestMapping("/rest/owner")
public class OwnerController {

    private final OwnerService ownerService;
    private final OwnerEndpointUtil ownerEndpointUtil;

    @Autowired
    public OwnerController(OwnerService ownerService, OwnerEndpointUtil ownerEndpointUtil) {
        this.ownerService = ownerService;
        this.ownerEndpointUtil = ownerEndpointUtil;
    }

    @GetMapping("/list")
    public ResponseEntity<List<OwnerDto>> getAll() {
        List<Owner> owners = ownerService.getAll();
        List<OwnerDto> dtos = ownerEndpointUtil.convertToDtoList(owners);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OwnerDto> getById(@PathVariable Long id) {
        Owner owner = ownerService.findById(id);
        if (owner == null) {
            return ResponseEntity.notFound().build();
        }
        OwnerDto dto = ownerEndpointUtil.convertToDto(owner);
        return ResponseEntity.ok(dto);
    }
}
