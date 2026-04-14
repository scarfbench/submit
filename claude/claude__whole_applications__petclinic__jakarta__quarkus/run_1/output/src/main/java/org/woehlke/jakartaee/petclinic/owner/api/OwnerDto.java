package org.woehlke.jakartaee.petclinic.owner.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import jakarta.xml.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.pet.api.PetListDto;

import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: tw
 * Date: 01.01.14
 * Time: 21:08
 * To change this template use File | Settings | File Templates.
 */
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

    @NotBlank
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

    @NotEmpty
    @JsonProperty
    private String addressInfo;

    @NotEmpty
    @JsonProperty
    private String city;

    @NotEmpty
    @JsonProperty
    @Digits(fraction = 0, integer = 5)
    private String zipCode;

    @NotEmpty
    @JsonProperty
    @Pattern(regexp = "\\+[1-9][0-9]{9,14}", message = "{invalid.phoneNumber}")
    private String phoneNumber;

    @JsonProperty
    private PetListDto petList = new PetListDto();

}
