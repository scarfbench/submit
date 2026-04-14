package org.woehlke.jakartaee.petclinic.specialty.api;

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
public class SpecialtyListDto implements Serializable {

    private static final long serialVersionUID = -5829352711555277375L;

    @JsonProperty("specialty")
    private List<SpecialtyDto> specialty = new ArrayList<>();
}
