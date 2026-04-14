package org.woehlke.jakartaee.petclinic.specialty.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpecialtyDto implements Serializable {

    private static final long serialVersionUID = -4631163068068039535L;

    @JsonProperty
    private Long id;

    @JsonProperty
    private UUID uuid;

    @JsonProperty
    private String name;
}
