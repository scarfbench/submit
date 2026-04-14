package org.woehlke.jakartaee.petclinic.visit.api;


import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.java.Log;
import org.woehlke.jakartaee.petclinic.visit.Visit;

import java.util.ArrayList;
import java.util.List;

@Log
@ApplicationScoped
public class VisitEndpointUtil {

    public VisitDto dtoFactory(Visit e) {
        VisitDto dto = new VisitDto();
        dto.setId(e.getId());
        dto.setUuid(e.getUuid());
        dto.setDate(e.getDate());
        dto.setDescription(e.getDescription());
        return dto;
    }

    public VisitListDto dtoListFactory(List<Visit> eList) {
        List<VisitDto> dtoList = new ArrayList<>();
        for (Visit e : eList) {
            VisitDto dto = this.dtoFactory(e);
            dtoList.add(dto);
        }
        return new VisitListDto(dtoList);
    }
}
