package org.woehlke.jakartaee.petclinic.pettype.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(
    name = "owner_pet_pettype",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_owner_pet_pettype_uuid", columnNames = {"uuid"})
    }
)
@NamedQueries({
    @NamedQuery(
        name = "PetType.getAll",
        query = "select pt from PetType pt order by pt.name"
    ),
    @NamedQuery(
        name = "PetType.searchIndex",
        query = "select pt from PetType pt where pt.searchindex like :searchterm order by pt.name"
    )
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PetType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "owner_pet_pettype_seq")
    @SequenceGenerator(name = "owner_pet_pettype_seq", sequenceName = "owner_pet_pettype_seq", allocationSize = 1)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "searchindex", length = 1024)
    private String searchindex;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.searchindex = this.name.toLowerCase();
    }
}
