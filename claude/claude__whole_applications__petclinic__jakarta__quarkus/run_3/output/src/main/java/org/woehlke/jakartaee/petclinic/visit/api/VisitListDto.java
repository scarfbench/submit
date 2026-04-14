package org.woehlke.jakartaee.petclinic.visit.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class VisitListDto implements Serializable {

    private static final long serialVersionUID = -5829352711555277375L;

    @JsonProperty("visit")
    private List<VisitDto> visit = new ArrayList<>();
}
