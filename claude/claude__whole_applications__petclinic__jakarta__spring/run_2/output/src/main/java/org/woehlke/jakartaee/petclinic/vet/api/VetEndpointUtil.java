package org.woehlke.jakartaee.petclinic.vet.api;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyDto;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyEndpointUtil;
import org.woehlke.jakartaee.petclinic.specialty.db.Specialty;
import org.woehlke.jakartaee.petclinic.vet.db.Vet;

import java.util.ArrayList;
import java.util.List;

@Log
@Component
public class VetEndpointUtil {

    private final SpecialtyEndpointUtil specialtyEndpointUtil;

    @Autowired
    public VetEndpointUtil(SpecialtyEndpointUtil specialtyEndpointUtil) {
        this.specialtyEndpointUtil = specialtyEndpointUtil;
    }

    public VetDto convertToDto(Vet vet) {
        if (vet == null) {
            return null;
        }

        VetDto dto = new VetDto();
        dto.setId(vet.getId());
        dto.setUuid(vet.getUuid());
        dto.setFirstName(vet.getFirstName());
        dto.setLastName(vet.getLastName());

        List<SpecialtyDto> specialtyDtos = new ArrayList<>();
        if (vet.getSpecialties() != null) {
            for (Specialty specialty : vet.getSpecialties()) {
                specialtyDtos.add(specialtyEndpointUtil.convertToDto(specialty));
            }
        }
        dto.setSpecialties(specialtyDtos);

        return dto;
    }

    public List<VetDto> convertToDtoList(List<Vet> vets) {
        List<VetDto> dtoList = new ArrayList<>();
        for (Vet vet : vets) {
            dtoList.add(convertToDto(vet));
        }
        return dtoList;
    }
}
