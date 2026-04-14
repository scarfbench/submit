package org.woehlke.jakartaee.petclinic.pet.api;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.pet.db.PetService;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeDto;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeEndpointUtil;
import org.woehlke.jakartaee.petclinic.visit.Visit;
import org.woehlke.jakartaee.petclinic.visit.api.VisitEndpointUtil;
import org.woehlke.jakartaee.petclinic.visit.api.VisitListDto;

import java.util.ArrayList;
import java.util.List;


@Log
@ApplicationScoped
public class PetEndpointUtil {

    @Inject
    private PetTypeEndpointUtil petTypeEndpointUtil;

    @Inject
    private VisitEndpointUtil visitEndpointUtil;

    @Inject
    private PetService petService;

    public PetDto dtoFactory(Pet pet) {
        PetDto dto = new PetDto();
        dto.setId(pet.getId());
        dto.setUuid(pet.getUuid());
        dto.setBirthDate(pet.getBirthDate());
        dto.setName(pet.getName());
        PetTypeDto oPetTypeDto =this.petTypeEndpointUtil.dtoFactory(pet.getType());
        dto.setPetType(oPetTypeDto);
        List<Visit> visitList = petService.getVisits(pet);
        VisitListDto oVisitListDto =this.visitEndpointUtil.dtoListFactory(visitList);
        dto.setVisitList(oVisitListDto);
        return dto;
    }

    public PetListDto dtoListFactory(List<Pet> petList) {
        List<PetDto> dtoList = new ArrayList<>();
        for (Pet pet : petList) {
            PetDto dto = this.dtoFactory(pet);
            dtoList.add(dto);
        }
        return new PetListDto(dtoList);
    }
}
