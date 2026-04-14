package org.woehlke.jakartaee.petclinic.pettype;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeDto;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeEndpointUtil;
import org.woehlke.jakartaee.petclinic.pettype.db.PetType;
import org.woehlke.jakartaee.petclinic.pettype.db.PetTypeService;

import java.util.List;

@Log
@RestController
@RequestMapping("/rest/petType")
public class PetTypeController {

    private final PetTypeService petTypeService;
    private final PetTypeEndpointUtil petTypeEndpointUtil;

    @Autowired
    public PetTypeController(PetTypeService petTypeService, PetTypeEndpointUtil petTypeEndpointUtil) {
        this.petTypeService = petTypeService;
        this.petTypeEndpointUtil = petTypeEndpointUtil;
    }

    @GetMapping("/list")
    public ResponseEntity<List<PetTypeDto>> getAll() {
        List<PetType> petTypes = petTypeService.getAll();
        List<PetTypeDto> dtos = petTypeEndpointUtil.convertToDtoList(petTypes);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetTypeDto> getById(@PathVariable Long id) {
        PetType petType = petTypeService.findById(id);
        if (petType == null) {
            return ResponseEntity.notFound().build();
        }
        PetTypeDto dto = petTypeEndpointUtil.convertToDto(petType);
        return ResponseEntity.ok(dto);
    }
}
