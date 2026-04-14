package org.woehlke.jakartaee.petclinic.specialty.api;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.woehlke.jakartaee.petclinic.specialty.db.Specialty;

import java.util.ArrayList;
import java.util.List;

@Log
@Component
public class SpecialtyEndpointUtil {

    public SpecialtyDto convertToDto(Specialty specialty) {
        if (specialty == null) {
            return null;
        }

        SpecialtyDto dto = new SpecialtyDto();
        dto.setId(specialty.getId());
        dto.setUuid(specialty.getUuid());
        dto.setName(specialty.getName());

        return dto;
    }

    public List<SpecialtyDto> convertToDtoList(List<Specialty> specialties) {
        List<SpecialtyDto> dtoList = new ArrayList<>();
        for (Specialty specialty : specialties) {
            dtoList.add(convertToDto(specialty));
        }
        return dtoList;
    }
}
