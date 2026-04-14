package roster.util;

import java.io.Serializable;

public class TeamDetails implements Serializable {
    private static final long serialVersionUID = -1618941013515364318L;
    private String id;
    private String name;
    private String city;

    public TeamDetails() {
    }

    public TeamDetails(String id, String name, String city) {
        this.id = id;
        this.name = name;
        this.city = city;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + city;
    }
}
