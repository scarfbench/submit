package org.woehlke.jakartaee.petclinic.specialty;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyDto;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyEndpointUtil;
import org.woehlke.jakartaee.petclinic.specialty.db.Specialty;
import org.woehlke.jakartaee.petclinic.specialty.db.SpecialtyService;

import java.util.List;

@Log
@RestController
@RequestMapping("/rest/specialty")
public class SpecialtyController {

    private final SpecialtyService specialtyService;
    private final SpecialtyEndpointUtil specialtyEndpointUtil;

    @Autowired
    public SpecialtyController(SpecialtyService specialtyService, SpecialtyEndpointUtil specialtyEndpointUtil) {
        this.specialtyService = specialtyService;
        this.specialtyEndpointUtil = specialtyEndpointUtil;
    }

    @GetMapping("/list")
    public ResponseEntity<List<SpecialtyDto>> getAll() {
        List<Specialty> specialties = specialtyService.getAll();
        List<SpecialtyDto> dtos = specialtyEndpointUtil.convertToDtoList(specialties);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialtyDto> getById(@PathVariable Long id) {
        Specialty specialty = specialtyService.findById(id);
        if (specialty == null) {
            return ResponseEntity.notFound().build();
        }
        SpecialtyDto dto = specialtyEndpointUtil.convertToDto(specialty);
        return ResponseEntity.ok(dto);
    }
}
