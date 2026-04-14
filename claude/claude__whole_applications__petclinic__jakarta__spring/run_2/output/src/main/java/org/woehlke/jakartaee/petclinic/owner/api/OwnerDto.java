package org.woehlke.jakartaee.petclinic.owner.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.woehlke.jakartaee.petclinic.pet.api.PetDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("address")
    private String address;

    @JsonProperty("houseNumber")
    private String houseNumber;

    @JsonProperty("addressInfo")
    private String addressInfo;

    @JsonProperty("city")
    private String city;

    @JsonProperty("zipCode")
    private String zipCode;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("petList")
    private List<PetDto> petList = new ArrayList<>();
}
