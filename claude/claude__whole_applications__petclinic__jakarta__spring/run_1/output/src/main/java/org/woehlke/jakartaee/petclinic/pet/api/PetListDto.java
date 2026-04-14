package org.woehlke.jakartaee.petclinic.pet.api;

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
public class PetListDto {

    @JsonProperty("pet")
    private List<PetDto> pet = new ArrayList<>();

}
