package org.woehlke.jakartaee.petclinic.visit.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.woehlke.jakartaee.petclinic.pet.db.Pet;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(
    name = "owner_pet_visit",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_owner_pet_visit_uuid", columnNames = {"uuid"})
    }
)
@NamedQueries({
    @NamedQuery(
        name = "Visit.getAll",
        query = "select v from Visit v order by v.date desc"
    ),
    @NamedQuery(
        name = "Visit.searchIndex",
        query = "select v from Visit v where v.searchindex like :searchterm order by v.date desc"
    )
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Visit implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "owner_pet_visit_seq")
    @SequenceGenerator(name = "owner_pet_visit_seq", sequenceName = "owner_pet_visit_seq", allocationSize = 1)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "searchindex", length = 1024)
    private String searchindex;

    @NotNull
    @Temporal(TemporalType.DATE)
    @Column(name = "visit_date", nullable = false)
    private Date date;

    @NotBlank
    @Column(name = "description", nullable = false, length = 2048)
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_pet_id", nullable = false)
    private Pet pet;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.searchindex = (this.description + " " +
                           (this.pet != null ? this.pet.getName() : "")).toLowerCase();
    }
}
