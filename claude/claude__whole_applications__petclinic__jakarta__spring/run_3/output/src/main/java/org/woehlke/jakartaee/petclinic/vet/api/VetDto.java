package org.woehlke.jakartaee.petclinic.vet.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyListDto;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class VetDto implements Serializable {

    private static final long serialVersionUID = 6749793465861123385L;

    @JsonProperty
    private Long id;

    @JsonProperty
    private UUID uuid;

    @JsonProperty
    private String firstName;

    @JsonProperty
    private String lastName;

    @JsonProperty
    private SpecialtyListDto specialtyList = new SpecialtyListDto();
}
