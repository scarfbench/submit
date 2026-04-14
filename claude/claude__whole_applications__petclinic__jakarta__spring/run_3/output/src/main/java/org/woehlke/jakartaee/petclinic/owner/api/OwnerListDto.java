package org.woehlke.jakartaee.petclinic.owner.api;

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
public class OwnerListDto implements Serializable {

    private static final long serialVersionUID = 7608980315748812643L;

    @JsonProperty
    private List<OwnerDto> owner = new ArrayList<>();
}
