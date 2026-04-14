package org.woehlke.jakartaee.petclinic.vet.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyListDto;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VetDto implements Serializable {

    private static final long serialVersionUID = -2215412510462397034L;

    @JsonProperty
    private Long id;

    @JsonProperty
    private UUID uuid;

    @JsonProperty
    private String firstName;

    @JsonProperty
    private String lastName;

    @JsonProperty("specialtyList")
    private SpecialtyListDto specialtyList = new SpecialtyListDto();
}
