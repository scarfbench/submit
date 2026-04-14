package org.woehlke.jakartaee.petclinic.pet.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.woehlke.jakartaee.petclinic.owner.db.Owner;
import org.woehlke.jakartaee.petclinic.pettype.db.PetType;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(
    name = "owner_pet",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_owner_pet_uuid", columnNames = {"uuid"})
    }
)
@NamedQueries({
    @NamedQuery(
        name = "Pet.getAll",
        query = "select p from Pet p order by p.name"
    ),
    @NamedQuery(
        name = "Pet.searchIndex",
        query = "select p from Pet p where p.searchindex like :searchterm order by p.name"
    )
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Pet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "owner_pet_seq")
    @SequenceGenerator(name = "owner_pet_seq", sequenceName = "owner_pet_seq", allocationSize = 1)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "searchindex", length = 1024)
    private String searchindex;

    @NotNull
    @Temporal(TemporalType.DATE)
    @Column(name = "birth_date", nullable = false)
    private Date birthDate;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_pet_pettype_id", nullable = false)
    private PetType type;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.searchindex = (this.name + " " +
                           (this.type != null ? this.type.getName() : "") + " " +
                           (this.owner != null ? this.owner.getFirstName() + " " + this.owner.getLastName() : "")).toLowerCase();
    }
}
