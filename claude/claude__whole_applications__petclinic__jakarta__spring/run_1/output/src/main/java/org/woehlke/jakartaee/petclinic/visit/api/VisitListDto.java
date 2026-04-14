package org.woehlke.jakartaee.petclinic.visit.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class VisitListDto {

    @JsonProperty("visit")
    private List<VisitDto> visit = new ArrayList<>();

}
