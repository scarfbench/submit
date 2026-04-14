package org.jakarta.samples.petclinic.owner;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import org.jakarta.samples.petclinic.model.Person;

@Entity
@Table(name = "owners")
public class Owner extends Person {

    @Column(name = "address")
    @NotEmpty
    public String address;

    @Column(name = "city")
    @NotEmpty
    public String city;

    @Column(name = "telephone")
    @NotEmpty
    @Digits(fraction = 0, integer = 10)
    public String telephone;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner", fetch = FetchType.EAGER)
    public Set<Pet> pets;

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public Set<Pet> getPets() { return getPetsInternal(); }
    public void setPets(Set<Pet> pets) { this.pets = pets; }

    public void addPet(Pet pet) {
        getPetsInternal().add(pet);
        pet.owner = this;
    }

    protected Set<Pet> getPetsInternal() {
        if (this.pets == null) {
            this.pets = new HashSet<>();
        }
        return this.pets;
    }

    @Override
    public String toString() {
        return "Owner [address=" + address + ", city=" + city + ", pets=" + pets + ", telephone=" + telephone + "]";
    }
}
