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

    private static final long serialVersionUID = -5829352711555277375L;

    @JsonProperty("vet")
    private List<VetDto> vet = new ArrayList<>();
}
