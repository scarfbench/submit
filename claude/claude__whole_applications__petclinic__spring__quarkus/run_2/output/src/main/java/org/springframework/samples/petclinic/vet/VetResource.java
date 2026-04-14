package org.springframework.samples.petclinic.vet;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class VetResource {

	@Inject
	VetRepository vetRepository;

	@Inject
	@Location("vets/vetList.html")
	Template vetList;

	@GET
	@Path("/vets.html")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance showVetList(@QueryParam("page") Integer page) {
		if (page == null) page = 1;

		int pageSize = 5;
		List<Vet> listVets = vetRepository.findAll(page, pageSize);
		long totalItems = vetRepository.count();
		int totalPages = (int) Math.ceil((double) totalItems / pageSize);

		List<Integer> pagesList = new ArrayList<>();
		for (int i = 1; i <= totalPages; i++) {
			pagesList.add(i);
		}

		return vetList
			.data("activeMenu", "vets")
			.data("listVets", listVets)
			.data("currentPage", page)
			.data("totalPages", totalPages)
			.data("totalItems", totalItems)
			.data("pages", pagesList);
	}

	@GET
	@Path("/vets")
	@Produces(MediaType.APPLICATION_JSON)
	public Vets showResourcesVetList() {
		Vets vets = new Vets();
		vets.getVetList().addAll(vetRepository.findAll());
		return vets;
	}

}
