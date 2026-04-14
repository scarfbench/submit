package org.springframework.samples.petclinic.vet;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Vets {

	private List<Vet> vets;

	@JsonProperty("vetList")
	public List<Vet> getVetList() {
		if (vets == null) {
			vets = new ArrayList<>();
		}
		return vets;
	}

}
