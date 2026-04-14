package roster.util;

import java.io.Serializable;

public class LeagueDetails implements Serializable {
    private static final long serialVersionUID = 290368886584321980L;
    private String id;
    private String name;
    private String sport;

    public LeagueDetails() {
    }

    public LeagueDetails(String id, String name, String sport) {
        this.id = id;
        this.name = name;
        this.sport = sport;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSport() {
        return sport;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + sport;
    }
}
