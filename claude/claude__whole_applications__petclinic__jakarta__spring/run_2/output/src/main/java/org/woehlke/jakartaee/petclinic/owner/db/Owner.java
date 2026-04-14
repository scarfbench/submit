package org.woehlke.jakartaee.petclinic.owner.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "owner",
    uniqueConstraints = {
        @UniqueConstraint(name = "owner_unique_uuid", columnNames = {"uuid"}),
        @UniqueConstraint(name = "owner_unique_email", columnNames = {"email"}),
        @UniqueConstraint(name = "owner_unique_names", columnNames = {"first_name", "lastName", "city", "phonenumber"})
    }
)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Owner implements Comparable<Owner>, Serializable {

    private static final long serialVersionUID = 7995827646591579744L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "owner_gen")
    @SequenceGenerator(name = "owner_gen", sequenceName = "owner_seq", allocationSize = 1)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private UUID uuid;

    @Column(name = "searchindex")
    private String searchindex;

    @NotEmpty
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotEmpty
    @Column(name = "lastName", nullable = false)
    private String lastName;

    @NotEmpty
    @Column(name = "address", nullable = false)
    private String address;

    @NotEmpty
    @Column(name = "housenumber", nullable = false)
    private String houseNumber;

    @Column(name = "address_info")
    private String addressInfo;

    @NotEmpty
    @Column(name = "city", nullable = false)
    private String city;

    @NotEmpty
    @Column(name = "zipcode", nullable = false)
    private String zipCode;

    @NotEmpty
    @Column(name = "phonenumber", nullable = false)
    private String phoneNumber;

    @NotEmpty
    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Transient
    public String getTableName() {
        return "owner";
    }

    @Transient
    public String getPrimaryKey() {
        return this.lastName + ", " + this.firstName;
    }

    @Transient
    public String getPrimaryKeyWithId() {
        return this.getPrimaryKey() + "(" + this.id + "," + this.uuid + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Owner)) return false;
        Owner owner = (Owner) o;
        return Objects.equals(firstName, owner.firstName) &&
               Objects.equals(lastName, owner.lastName) &&
               Objects.equals(email, owner.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email);
    }

    @Override
    public int compareTo(Owner o) {
        return (this.lastName + this.firstName).compareTo(o.lastName + o.firstName);
    }
}
