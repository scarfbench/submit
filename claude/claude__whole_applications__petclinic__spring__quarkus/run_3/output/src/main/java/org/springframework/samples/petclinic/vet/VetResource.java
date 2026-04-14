package org.springframework.samples.petclinic.vet;

import java.util.List;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
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
	Template vetList;

	@GET
	@Path("/vets.html")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance showVetList(@QueryParam("page") @DefaultValue("1") int page) {
		int pageSize = 5;
		List<Vet> listVets = vetRepository.findAll(page, pageSize);
		long totalItems = vetRepository.count();
		int totalPages = (int) Math.ceil((double) totalItems / pageSize);

		return vetList.data("menu", "vets")
				.data("listVets", listVets)
				.data("currentPage", page)
				.data("totalPages", totalPages)
				.data("totalItems", totalItems);
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
