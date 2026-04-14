package org.woehlke.jakartaee.petclinic.vet.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class VetListDto implements Serializable {

    private static final long serialVersionUID = 6396791677094922721L;

    @JsonProperty
    private List<VetDto> vetList = new ArrayList<>();
}
