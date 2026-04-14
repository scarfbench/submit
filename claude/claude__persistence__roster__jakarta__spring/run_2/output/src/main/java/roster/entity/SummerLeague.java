package roster.entity;

import java.io.Serializable;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import roster.util.IncorrectSportException;

@Entity
@DiscriminatorValue("SUMMER")
public class SummerLeague extends League implements Serializable {
    private static final long serialVersionUID = 4846138039113922695L;

    public SummerLeague() {
    }

    public SummerLeague(String id, String name, String sport)
            throws IncorrectSportException {
        this.setId(id);
        this.setName(name);
        if (sport.equalsIgnoreCase("swimming") ||
                sport.equalsIgnoreCase("soccer") ||
                sport.equalsIgnoreCase("basketball") ||
                sport.equalsIgnoreCase("baseball")) {
            this.setSport(sport);
        } else {
            throw new IncorrectSportException("Sport is not a summer sport.");
        }
    }
}
