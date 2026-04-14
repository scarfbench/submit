package org.woehlke.jakartaee.petclinic.owner.api;

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
public class OwnerListDto {

    @JsonProperty
    private List<OwnerDto> owner = new ArrayList<>();

}
