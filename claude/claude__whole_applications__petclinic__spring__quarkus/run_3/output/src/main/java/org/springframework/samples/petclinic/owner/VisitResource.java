package org.springframework.samples.petclinic.owner;

import java.net.URI;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/owners")
public class VisitResource {

	@Inject
	OwnerRepository owners;

	@Inject
	Template createOrUpdateVisitForm;

	@GET
	@Path("/{ownerId: [0-9]+}/pets/{petId: [0-9]+}/visits/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initNewVisitForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId) {
		Owner owner = owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		Pet pet = owner.getPet(petId);
		Visit visit = new Visit();
		return createOrUpdateVisitForm.data("menu", "owners")
				.data("owner", owner)
				.data("pet", pet)
				.data("visit", visit);
	}

	@POST
	@Path("/{ownerId: [0-9]+}/pets/{petId: [0-9]+}/visits/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processNewVisitForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId,
			@FormParam("date") String dateStr,
			@FormParam("description") String description) {

		Owner owner = owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		Pet pet = owner.getPet(petId);

		Visit visit = new Visit();
		if (dateStr != null && !dateStr.isBlank()) {
			visit.setDate(java.time.LocalDate.parse(dateStr));
		}
		visit.setDescription(description);

		// Validation
		if (description == null || description.isBlank()) {
			return createOrUpdateVisitForm.data("menu", "owners")
					.data("owner", owner)
					.data("pet", pet)
					.data("visit", visit)
					.data("error", "Description is required.");
		}

		owner.addVisit(petId, visit);
		owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

}
