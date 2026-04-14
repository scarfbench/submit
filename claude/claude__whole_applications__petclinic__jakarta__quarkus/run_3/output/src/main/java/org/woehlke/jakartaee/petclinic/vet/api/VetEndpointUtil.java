package org.woehlke.jakartaee.petclinic.vet.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyDto;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyEndpointUtil;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyListDto;
import org.woehlke.jakartaee.petclinic.vet.Vet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log
@ApplicationScoped
public class VetEndpointUtil {

    @Inject
    SpecialtyEndpointUtil specialtyEndpointUtil;

    public VetDto dtoFactory(Vet vet) {
        VetDto dto = new VetDto();
        dto.setId(vet.getId());
        dto.setUuid(vet.getUuid());
        dto.setFirstName(vet.getFirstName());
        dto.setLastName(vet.getLastName());
        Set<Specialty> specialties = vet.getSpecialties();
        List<SpecialtyDto> specialtyDtoList = new ArrayList<>();
        if (specialties != null) {
            for (Specialty specialty : specialties) {
                specialtyDtoList.add(specialtyEndpointUtil.dtoFactory(specialty));
            }
        }
        dto.setSpecialtyList(new SpecialtyListDto(specialtyDtoList));
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
