package org.woehlke.jakartaee.petclinic.pettype.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PetTypeListDto {

    @JsonProperty
    private List<PetTypeDto> petType = new ArrayList<>();

}
