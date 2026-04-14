package org.woehlke.jakartaee.petclinic.visit;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.woehlke.jakartaee.petclinic.application.framework.EntityBase;
import org.woehlke.jakartaee.petclinic.application.framework.EntityBaseObject;
import org.woehlke.jakartaee.petclinic.pet.Pet;
import org.woehlke.jakartaee.petclinic.visit.db.VisitListener;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.*;

@Entity
@Getter
@Setter
@ToString(exclude = {"pet"})
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = Visit.TABLENAME,
        uniqueConstraints = {
                @UniqueConstraint(name = Visit.TABLENAME + "_unique_uuid", columnNames = {Visit.COL_UUID})
        }
)
@NamedQueries({
        @NamedQuery(name = "Visit.getAll", query = "select v from Visit v order by v.date desc"),
        @NamedQuery(name = "Visit.getVisits", query = "select v from Visit v where v.pet=:pet order by v.date desc")
})
@EntityListeners(VisitListener.class)
public class Visit extends EntityBaseObject implements EntityBase, Comparable<Visit>, Serializable {

    public final static String TABLENAME = "owner_pet_visit";
    public final static String COL_ID = "id";
    public final static String COL_UUID = "uuid";
    public final static String COL_DATE = "visit_date";
    public final static String COL_DESCRIPTION = "description";
    public final static String COL_PET_ID = "owner_pet_id";
    public final static String COL_SEARCHINDEX = "searchindex";
    private static final long serialVersionUID = 6872877779662856834L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "owner_pet_visit_gen")
    @SequenceGenerator(name = "owner_pet_visit_gen", sequenceName = "owner_pet_visit_seq")
    private Long id;

    @Column(name = COL_UUID, nullable = false, unique = true, length = 36)
    private UUID uuid;

    @Column(name = COL_SEARCHINDEX)
    private String searchindex;

    @NotNull
    @Column(name = COL_DATE, columnDefinition = "DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @NotEmpty
    @Column(name = COL_DESCRIPTION, nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.DETACH, CascadeType.MERGE}, targetEntity = Pet.class)
    @JoinColumn(name = COL_PET_ID)
    private Pet pet;

    @Transient
    public String getTableName() { return TABLENAME; }

    @Transient
    public String getPrimaryKey() { return this.getDescription(); }

    @Transient
    public String getPrimaryKeyWithId() { return this.getPrimaryKey() + "(" + this.getId() + "," + this.getUuid() + ")"; }

    @Override
    public void updateSearchindex() {
        StringBuilder b = new StringBuilder();
        if (this.getDate() != null) { b.append(this.getDate().toInstant().toString()); b.append(" "); }
        if (this.getDescription() != null) {
            for (String e : this.getDescription().split("\\W")) { b.append(e); b.append(" "); }
        }
        this.setSearchindex(b.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Visit)) return false;
        Visit visit = (Visit) o;
        return Objects.equals(getDate(), visit.getDate()) && Objects.equals(getDescription(), visit.getDescription()) && Objects.equals(getPet(), visit.getPet());
    }

    @Override
    public int hashCode() { return Objects.hash(getDate(), getDescription()); }

    @Override
    public int compareTo(Visit o) { return this.getDate().compareTo(o.getDate()); }
}
