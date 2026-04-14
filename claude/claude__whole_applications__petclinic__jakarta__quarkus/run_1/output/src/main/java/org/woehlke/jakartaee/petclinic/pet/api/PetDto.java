package org.woehlke.jakartaee.petclinic.pet.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.woehlke.jakartaee.petclinic.pettype.api.PetTypeDto;

import jakarta.xml.bind.annotation.*;
import org.woehlke.jakartaee.petclinic.visit.api.VisitListDto;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;


/**
 * Created with IntelliJ IDEA.
 * User: tw
 * Date: 01.01.14
 * Time: 21:09
 * To change this template use File | Settings | File Templates.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "pet")
public class PetDto implements Serializable {

    private static final long serialVersionUID = 1007513582768464905L;

    @NotNull
    @JsonProperty
    private Long id;

    @NotBlank
    @JsonProperty
    private UUID uuid;

    @NotEmpty
    @JsonProperty
    private String name;

    @NotNull
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date birthDate;

    @NotNull
    @JsonProperty
    private PetTypeDto petType;

    @NotNull
    @JsonProperty("visitList")
    private VisitListDto visitList = new VisitListDto();

}
