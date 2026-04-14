package org.woehlke.jakartaee.petclinic.pettype.api;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.woehlke.jakartaee.petclinic.pettype.db.PetType;

import java.util.ArrayList;
import java.util.List;

@Log
@Component
public class PetTypeEndpointUtil {

    public PetTypeDto convertToDto(PetType petType) {
        if (petType == null) {
            return null;
        }

        PetTypeDto dto = new PetTypeDto();
        dto.setId(petType.getId());
        dto.setUuid(petType.getUuid());
        dto.setName(petType.getName());

        return dto;
    }

    public List<PetTypeDto> convertToDtoList(List<PetType> petTypes) {
        List<PetTypeDto> dtoList = new ArrayList<>();
        for (PetType petType : petTypes) {
            dtoList.add(convertToDto(petType));
        }
        return dtoList;
    }
}
