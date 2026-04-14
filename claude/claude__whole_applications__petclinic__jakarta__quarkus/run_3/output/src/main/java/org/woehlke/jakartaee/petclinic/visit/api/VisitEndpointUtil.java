package org.woehlke.jakartaee.petclinic.visit.api;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.ArrayList;
import java.util.List;

@Log
@ApplicationScoped
public class VisitEndpointUtil {

    public VisitDto dtoFactory(Visit visit) {
        VisitDto dto = new VisitDto();
        dto.setId(visit.getId());
        dto.setUuid(visit.getUuid());
        dto.setDate(visit.getDate());
        dto.setDescription(visit.getDescription());
        return dto;
    }

    public VisitListDto dtoListFactory(List<Visit> visitList) {
        List<VisitDto> dtoList = new ArrayList<>();
        for (Visit visit : visitList) {
            VisitDto dto = this.dtoFactory(visit);
            dtoList.add(dto);
        }
        return new VisitListDto(dtoList);
    }
}
