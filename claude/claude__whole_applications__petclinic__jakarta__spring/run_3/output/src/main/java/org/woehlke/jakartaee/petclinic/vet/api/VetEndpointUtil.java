package org.woehlke.jakartaee.petclinic.vet.api;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyEndpointUtil;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyListDto;
import org.woehlke.jakartaee.petclinic.vet.Vet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log
@Component
public class VetEndpointUtil {

    private final SpecialtyEndpointUtil specialtyEndpointUtil;

    @Autowired
    public VetEndpointUtil(SpecialtyEndpointUtil specialtyEndpointUtil) {
        this.specialtyEndpointUtil = specialtyEndpointUtil;
    }

    public VetDto dtoFactory(Vet vet) {
        VetDto dto = new VetDto();
        dto.setId(vet.getId());
        dto.setUuid(vet.getUuid());
        dto.setFirstName(vet.getFirstName());
        dto.setLastName(vet.getLastName());
        Set<Specialty> specialties = vet.getSpecialties();
        List<Specialty> specialtyList = new ArrayList<>(specialties);
        SpecialtyListDto specialtyDtoList = this.specialtyEndpointUtil.dtoListFactory(specialtyList);
        dto.setSpecialtyList(specialtyDtoList);
        return dto;
    }

    public VetListDto dtoListFactory(List<Vet> vetList) {
        List<VetDto> dtoList = new ArrayList<>();
        for (Vet vet : vetList) {
            VetDto dto = this.dtoFactory(vet);
            dtoList.add(dto);
        }
        return new VetListDto(dtoList);
    }
}
