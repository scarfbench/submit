package org.quarkus.samples.petclinic.owner;

public class OwnerForm {

    public String firstName;
    public String lastName;

    @Override
    public String toString() {
        return "OwnerForm [firstName=" + firstName + ", lastName=" + lastName + "]";
    }
}
