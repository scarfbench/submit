package org.woehlke.jakartaee.petclinic.pettype.api;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.woehlke.jakartaee.petclinic.pettype.PetType;

import java.util.ArrayList;
import java.util.List;

@Log
@Component
public class PetTypeEndpointUtil {

    public PetTypeDto dtoFactory(PetType petType) {
        PetTypeDto dto = new PetTypeDto();
        dto.setId(petType.getId());
        dto.setUuid(petType.getUuid());
        dto.setName(petType.getName());
        return dto;
    }

    public PetTypeListDto dtoListFactory(List<PetType> petTypeList) {
        List<PetTypeDto> dtoList = new ArrayList<>();
        for (PetType petType : petTypeList) {
            PetTypeDto dto = this.dtoFactory(petType);
            dtoList.add(dto);
        }
        return new PetTypeListDto(dtoList);
    }
}
