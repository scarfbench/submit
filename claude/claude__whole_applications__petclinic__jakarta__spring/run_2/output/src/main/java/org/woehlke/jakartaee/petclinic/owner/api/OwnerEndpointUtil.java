package org.woehlke.jakartaee.petclinic.owner.api;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.woehlke.jakartaee.petclinic.owner.db.Owner;
import org.woehlke.jakartaee.petclinic.pet.api.PetDto;
import org.woehlke.jakartaee.petclinic.pet.api.PetEndpointUtil;
import org.woehlke.jakartaee.petclinic.pet.db.Pet;
import org.woehlke.jakartaee.petclinic.pet.db.PetRepository;

import java.util.ArrayList;
import java.util.List;

@Log
@Component
public class OwnerEndpointUtil {

    private final PetRepository petRepository;
    private final PetEndpointUtil petEndpointUtil;

    @Autowired
    public OwnerEndpointUtil(PetRepository petRepository, PetEndpointUtil petEndpointUtil) {
        this.petRepository = petRepository;
        this.petEndpointUtil = petEndpointUtil;
    }

    public OwnerDto convertToDto(Owner owner) {
        if (owner == null) {
            return null;
        }

        OwnerDto dto = new OwnerDto();
        dto.setId(owner.getId());
        dto.setUuid(owner.getUuid());
        dto.setFirstName(owner.getFirstName());
        dto.setLastName(owner.getLastName());
        dto.setAddress(owner.getAddress());
        dto.setHouseNumber(owner.getHouseNumber());
        dto.setAddressInfo(owner.getAddressInfo());
        dto.setCity(owner.getCity());
        dto.setZipCode(owner.getZipCode());
        dto.setPhoneNumber(owner.getPhoneNumber());
        dto.setEmail(owner.getEmail());

        List<Pet> pets = petRepository.findByOwnerOrderByNameAsc(owner);
        List<PetDto> petDtos = new ArrayList<>();
        for (Pet pet : pets) {
            petDtos.add(petEndpointUtil.convertToDto(pet));
        }
        dto.setPetList(petDtos);

        return dto;
    }

    public List<OwnerDto> convertToDtoList(List<Owner> owners) {
        List<OwnerDto> dtoList = new ArrayList<>();
        for (Owner owner : owners) {
            dtoList.add(convertToDto(owner));
        }
        return dtoList;
    }
}
