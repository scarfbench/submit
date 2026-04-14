package org.woehlke.jakartaee.petclinic.vet;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.woehlke.jakartaee.petclinic.application.framework.EntityBase;
import org.woehlke.jakartaee.petclinic.application.framework.EntityBaseObject;
import org.woehlke.jakartaee.petclinic.specialty.Specialty;
import org.woehlke.jakartaee.petclinic.vet.db.VetListener;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = Vet.TABLENAME,
        uniqueConstraints = {
                @UniqueConstraint(name = Vet.TABLENAME + "_unique_uuid", columnNames = {Vet.COL_UUID}),
                @UniqueConstraint(name = Vet.TABLENAME + "_unique_names", columnNames = {Vet.COL_FIRSTNAME, Vet.COL_LASTNAME})
        }
)
@NamedQueries({
        @NamedQuery(name = "Vet.getAll", query = "select v from Vet v order by v.lastName,v.firstName asc"),
        @NamedQuery(name = "Vet.search", query = "select v from Vet v where v.searchindex like :searchterm order by v.lastName,v.firstName asc")
})
@EntityListeners(VetListener.class)
public class Vet extends EntityBaseObject implements EntityBase, Comparable<Vet>, Serializable {

    public final static String TABLENAME = "vet";
    public final static String COL_ID = "id";
    public final static String COL_UUID = "uuid";
    public final static String COL_FIRSTNAME = "first_name";
    public final static String COL_LASTNAME = "lastname";
    public final static String COL_SEARCHINDEX = "searchindex";
    private static final long serialVersionUID = -2215412510462397034L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vet_gen")
    @SequenceGenerator(name = "vet_gen", sequenceName = "vet_seq")
    private Long id;

    @Column(name = COL_UUID, nullable = false, unique = true, length = 36)
    private UUID uuid;

    @Column(name = COL_SEARCHINDEX)
    private String searchindex;

    @NotEmpty
    @Column(name = COL_FIRSTNAME, nullable = false)
    private String firstName;

    @NotEmpty
    @Column(name = COL_LASTNAME, nullable = false)
    private String lastName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "vet_specialties",
            joinColumns = @JoinColumn(name = "vet_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    private Set<Specialty> specialties = new TreeSet<>();

    @Transient
    public String getTableName() { return TABLENAME; }

    @Transient
    public String getPrimaryKey() { return this.lastName + ", " + this.firstName; }

    @Transient
    public String getPrimaryKeyWithId() { return this.getPrimaryKey() + "(" + this.getId() + "," + this.getUuid() + ")"; }

    @Transient
    public String getSpecialtiesAsString() {
        StringBuilder s = new StringBuilder();
        if (this.specialties != null) {
            for (Specialty specialty : this.specialties) {
                s.append(specialty.getName()).append(" ");
            }
        }
        return s.toString();
    }

    @Override
    public void updateSearchindex() {
        List<String> l = new ArrayList<>();
        l.add(this.getFirstName());
        l.add(this.getLastName());
        l.add(this.getSpecialtiesAsString());
        StringBuilder b = new StringBuilder();
        for (String ll : l) {
            for (String e : ll.split("\\W")) { b.append(e); b.append(" "); }
        }
        this.setSearchindex(b.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vet)) return false;
        Vet vet = (Vet) o;
        return getFirstName().equals(vet.getFirstName()) && getLastName().equals(vet.getLastName());
    }

    @Override
    public int hashCode() { return Objects.hash(getFirstName(), getLastName()); }

    @Override
    public int compareTo(Vet o) { return this.getPrimaryKey().compareTo(o.getPrimaryKey()); }
}
