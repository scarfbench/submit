package org.springframework.samples.petclinic.vet;

import java.util.ArrayList;
import java.util.List;

public class Vets {

    private List<Vet> vetList;

    public List<Vet> getVetList() {
        if (vetList == null) {
            vetList = new ArrayList<>();
        }
        return vetList;
    }

    public void setVetList(List<Vet> vetList) {
        this.vetList = vetList;
    }
}
