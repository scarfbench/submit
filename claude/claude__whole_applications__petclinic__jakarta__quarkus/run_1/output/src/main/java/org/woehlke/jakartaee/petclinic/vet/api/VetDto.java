package org.woehlke.jakartaee.petclinic.vet.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import jakarta.xml.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.specialty.api.SpecialtyListDto;

import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: tw
 * Date: 01.01.14
 * Time: 21:10
 * To change this template use File | Settings | File Templates.
 */
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

    @NotBlank
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
