package org.woehlke.jakartaee.petclinic.specialty.api;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;

import java.util.ArrayList;
import java.util.List;

@Log
@ApplicationScoped
public class SpecialtyEndpointUtil {

    public SpecialtyDto dtoFactory(Specialty specialty) {
        SpecialtyDto dto = new SpecialtyDto();
        dto.setId(specialty.getId());
        dto.setUuid(specialty.getUuid());
        dto.setName(specialty.getName());
        return dto;
    }

    public SpecialtyListDto dtoListFactory(List<Specialty> specialtyList) {
        List<SpecialtyDto> dtoList = new ArrayList<>();
        for (Specialty petType : specialtyList) {
            SpecialtyDto dto = this.dtoFactory(petType);
            dtoList.add(dto);
        }
        return new SpecialtyListDto(dtoList);
    }

}
