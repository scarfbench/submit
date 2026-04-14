package org.woehlke.jakartaee.petclinic.pettype.api;

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
public class PetTypeDto implements Serializable {

    private static final long serialVersionUID = -2213412509142145275L;

    @JsonProperty
    private Long id;

    @JsonProperty
    private UUID uuid;

    @JsonProperty
    private String name;
}
