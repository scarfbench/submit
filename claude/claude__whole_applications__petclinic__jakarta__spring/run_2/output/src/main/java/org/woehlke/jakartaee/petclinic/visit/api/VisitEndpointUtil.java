package org.woehlke.jakartaee.petclinic.visit.api;

import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.woehlke.jakartaee.petclinic.visit.db.Visit;

import java.util.ArrayList;
import java.util.List;

@Log
@Component
public class VisitEndpointUtil {

    public VisitDto convertToDto(Visit visit) {
        if (visit == null) {
            return null;
        }

        VisitDto dto = new VisitDto();
        dto.setId(visit.getId());
        dto.setUuid(visit.getUuid());
        dto.setDate(visit.getDate());
        dto.setDescription(visit.getDescription());

        if (visit.getPet() != null) {
            dto.setPetId(visit.getPet().getId());
            dto.setPetName(visit.getPet().getName());
        }

        return dto;
    }

    public List<VisitDto> convertToDtoList(List<Visit> visits) {
        List<VisitDto> dtoList = new ArrayList<>();
        for (Visit visit : visits) {
            dtoList.add(convertToDto(visit));
        }
        return dtoList;
    }
}
