package org.woehlke.jakartaee.petclinic.pet.api;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.woehlke.jakartaee.petclinic.pet.db.Pet;

import java.util.ArrayList;
import java.util.List;

@Log
@Component
public class PetEndpointUtil {

    public PetDto convertToDto(Pet pet) {
        if (pet == null) {
            return null;
        }

        PetDto dto = new PetDto();
        dto.setId(pet.getId());
        dto.setUuid(pet.getUuid());
        dto.setName(pet.getName());
        dto.setBirthDate(pet.getBirthDate());

        if (pet.getType() != null) {
            dto.setPetTypeId(pet.getType().getId());
            dto.setPetTypeName(pet.getType().getName());
        }

        return dto;
    }

    public List<PetDto> convertToDtoList(List<Pet> pets) {
        List<PetDto> dtoList = new ArrayList<>();
        for (Pet pet : pets) {
            dtoList.add(convertToDto(pet));
        }
        return dtoList;
    }
}
