package org.woehlke.jakartaee.petclinic.specialty.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(
    name = "specialty",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_specialty_uuid", columnNames = {"uuid"})
    }
)
@NamedQueries({
    @NamedQuery(
        name = "Specialty.getAll",
        query = "select s from Specialty s order by s.name"
    ),
    @NamedQuery(
        name = "Specialty.searchIndex",
        query = "select s from Specialty s where s.searchindex like :searchterm order by s.name"
    )
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Specialty implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "specialty_seq")
    @SequenceGenerator(name = "specialty_seq", sequenceName = "specialty_seq", allocationSize = 1)
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
