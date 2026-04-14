package org.woehlke.jakartaee.petclinic.specialty;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.woehlke.jakartaee.petclinic.application.framework.EntityBase;
import org.woehlke.jakartaee.petclinic.application.framework.EntityBaseObject;
import org.woehlke.jakartaee.petclinic.specialty.db.SpecialtyListener;

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
        name = Specialty.TABLENAME,
        uniqueConstraints = {
                @UniqueConstraint(name = Specialty.TABLENAME + "_unique_uuid", columnNames = {Specialty.COL_UUID}),
                @UniqueConstraint(name = Specialty.TABLENAME + "_unique_names", columnNames = {Specialty.COL_NAME})
        }
)
@NamedQueries({
        @NamedQuery(name = "Specialty.getAll", query = "select s from Specialty s order by s.name asc"),
        @NamedQuery(name = "Specialty.findSpecialtyByName", query = "select s from Specialty s where s.name=:name"),
        @NamedQuery(name = "Specialty.search", query = "select s from Specialty s where s.searchindex like :searchterm order by s.name asc")
})
@EntityListeners(SpecialtyListener.class)
public class Specialty extends EntityBaseObject implements EntityBase, Comparable<Specialty>, Serializable {

    public final static String TABLENAME = "specialty";
    public final static String COL_ID = "id";
    public final static String COL_UUID = "uuid";
    public final static String COL_NAME = "name";
    public final static String COL_SEARCHINDEX = "searchindex";
    private static final long serialVersionUID = -4631163068068039535L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "specialty_gen")
    @SequenceGenerator(name = "specialty_gen", sequenceName = "specialty_seq")
    private Long id;

    @Column(name = COL_UUID, nullable = false, unique = true, length = 36)
    private UUID uuid;

    @Column(name = COL_SEARCHINDEX)
    private String searchindex;

    @NotEmpty
    @Column(name = COL_NAME, unique = true, nullable = false)
    private String name;

    @Transient
    public String getTableName() { return TABLENAME; }

    @Transient
    public String getPrimaryKey() { return this.getName(); }

    @Transient
    public String getPrimaryKeyWithId() { return this.getPrimaryKey() + "(" + this.getId() + "," + this.getUuid() + ")"; }

    @Override
    public void updateSearchindex() {
        String[] element = this.getName().split("\\W");
        StringBuilder b = new StringBuilder();
        for (String e : element) { b.append(e); b.append(" "); }
        this.setSearchindex(b.toString());
    }

    public static Specialty newEntity() {
        Specialty o = new Specialty();
        o.setUuid(UUID.randomUUID());
        return o;
    }

    public static Specialty newEntity(String name) {
        Specialty o = new Specialty();
        o.setUuid(UUID.randomUUID());
        o.setName(name);
        return o;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Specialty)) return false;
        Specialty specialty = (Specialty) o;
        return getName().equals(specialty.getName());
    }

    @Override
    public int hashCode() { return Objects.hash(getName()); }

    @Override
    public int compareTo(Specialty o) { return this.getName().compareTo(o.getName()); }
}
