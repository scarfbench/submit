package org.woehlke.jakartaee.petclinic.specialty.api;

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
public class SpecialtyDto implements Serializable {

    private static final long serialVersionUID = -836560513920170089L;

    @JsonProperty
    private Long id;

    @JsonProperty
    private UUID uuid;

    @JsonProperty
    private String name;
}
