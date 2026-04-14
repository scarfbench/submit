package org.woehlke.jakartaee.petclinic.vet.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.woehlke.jakartaee.petclinic.specialty.db.Specialty;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
    name = "vet",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_vet_uuid", columnNames = {"uuid"})
    }
)
@NamedQueries({
    @NamedQuery(
        name = "Vet.getAll",
        query = "select v from Vet v order by v.lastName, v.firstName"
    ),
    @NamedQuery(
        name = "Vet.searchIndex",
        query = "select v from Vet v where v.searchindex like :searchterm order by v.lastName, v.firstName"
    )
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Vet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vet_seq")
    @SequenceGenerator(name = "vet_seq", sequenceName = "vet_seq", allocationSize = 1)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "searchindex", length = 1024)
    private String searchindex;

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "lastName", nullable = false)
    private String lastName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "vet_specialties",
        joinColumns = @JoinColumn(name = "vet_id"),
        inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    @ToString.Exclude
    private Set<Specialty> specialties = new HashSet<>();

    @PrePersist
    @PreUpdate
    public void prePersist() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.firstName).append(" ").append(this.lastName);
        if (this.specialties != null) {
            for (Specialty specialty : this.specialties) {
                sb.append(" ").append(specialty.getName());
            }
        }
        this.searchindex = sb.toString().toLowerCase();
    }
}
