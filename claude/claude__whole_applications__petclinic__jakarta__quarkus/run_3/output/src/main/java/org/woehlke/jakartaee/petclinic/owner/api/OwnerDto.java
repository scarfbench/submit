package org.woehlke.jakartaee.petclinic.owner.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.woehlke.jakartaee.petclinic.pet.api.PetListDto;

import java.io.Serializable;
import java.util.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OwnerDto implements Serializable {

    private static final long serialVersionUID = 7995827646591579744L;

    @JsonProperty
    private Long id;

    @JsonProperty
    private UUID uuid;

    @JsonProperty
    private String firstName;

    @JsonProperty
    private String lastName;

    @JsonProperty
    private String address;

    @JsonProperty
    private String houseNumber;

    @JsonProperty
    private String addressInfo;

    @JsonProperty
    private String city;

    @JsonProperty
    private String zipCode;

    @JsonProperty
    private String phoneNumber;

    @JsonProperty
    private PetListDto petList = new PetListDto();
}
