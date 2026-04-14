package org.woehlke.jakartaee.petclinic.vet.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import jakarta.xml.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyListDto;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "Vet")
public class VetDto implements Serializable {

    private static final long serialVersionUID = 6749793465861123385L;

    @NotNull
    @JsonProperty
    private Long id;

    @NotNull
    @JsonProperty
    private UUID uuid;

    @NotEmpty
    @JsonProperty
    private String firstName;

    @NotEmpty
    @JsonProperty
    private String lastName;

    @NotNull
    @JsonProperty
    private SpecialtyListDto specialtyList = new SpecialtyListDto();
}
