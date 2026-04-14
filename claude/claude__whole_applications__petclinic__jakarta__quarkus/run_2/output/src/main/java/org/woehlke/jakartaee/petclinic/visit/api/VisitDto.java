package org.woehlke.jakartaee.petclinic.visit.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import jakarta.xml.bind.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "visit")
public class VisitDto implements Serializable {

    private static final long serialVersionUID = 2357446696894656827L;

    @NotNull
    @JsonProperty
    private Long id;

    @NotNull
    @JsonProperty
    private UUID uuid;

    @NotNull
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    protected Date date;

    @NotEmpty
    @JsonProperty
    private String description;
}
