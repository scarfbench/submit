package org.woehlke.jakartaee.petclinic.vet;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.vet.api.VetDto;
import org.woehlke.jakartaee.petclinic.vet.api.VetEndpointUtil;
import org.woehlke.jakartaee.petclinic.vet.db.Vet;
import org.woehlke.jakartaee.petclinic.vet.db.VetService;

import java.util.List;

@Log
@RestController
@RequestMapping("/rest/vet")
public class VetController {

    private final VetService vetService;
    private final VetEndpointUtil vetEndpointUtil;

    @Autowired
    public VetController(VetService vetService, VetEndpointUtil vetEndpointUtil) {
        this.vetService = vetService;
        this.vetEndpointUtil = vetEndpointUtil;
    }

    @GetMapping("/list")
    public ResponseEntity<List<VetDto>> getAll() {
        List<Vet> vets = vetService.getAll();
        List<VetDto> dtos = vetEndpointUtil.convertToDtoList(vets);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VetDto> getById(@PathVariable Long id) {
        Vet vet = vetService.findById(id);
        if (vet == null) {
            return ResponseEntity.notFound().build();
        }
        VetDto dto = vetEndpointUtil.convertToDto(vet);
        return ResponseEntity.ok(dto);
    }
}
