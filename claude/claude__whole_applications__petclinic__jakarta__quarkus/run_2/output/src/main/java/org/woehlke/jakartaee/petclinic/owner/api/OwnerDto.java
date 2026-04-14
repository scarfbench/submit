package org.woehlke.jakartaee.petclinic.owner.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import jakarta.xml.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.pet.api.PetListDto;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "owner")
public class OwnerDto implements Serializable {

    private static final long serialVersionUID = 7995827646591579744L;

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

    @NotEmpty
    @JsonProperty
    private String address;

    @NotEmpty
    @JsonProperty
    private String houseNumber;

    @JsonProperty
    private String addressInfo;

    @NotEmpty
    @JsonProperty
    private String city;

    @NotEmpty
    @JsonProperty
    private String zipCode;

    @NotEmpty
    @JsonProperty
    private String phoneNumber;

    @JsonProperty
    private PetListDto petList = new PetListDto();
}
