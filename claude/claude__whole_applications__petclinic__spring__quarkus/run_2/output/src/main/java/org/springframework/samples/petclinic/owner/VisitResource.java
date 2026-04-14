package org.springframework.samples.petclinic.owner;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.quarkus.qute.Location;
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

@Path("/owners/{ownerId}/pets/{petId}/visits")
public class VisitResource {

	@Inject
	OwnerRepository ownerRepository;

	@Inject
	@Location("pets/createOrUpdateVisitForm.html")
	Template createOrUpdateVisitForm;

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public Response initNewVisitForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId) {

		Optional<Owner> optionalOwner = ownerRepository.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		Visit visit = new Visit();

		TemplateInstance template = createOrUpdateVisitForm
			.data("activeMenu", "owners")
			.data("owner", owner)
			.data("pet", pet)
			.data("visit", visit);
		return Response.ok(template).build();
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Response processNewVisitForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId,
			@FormParam("date") String dateStr,
			@FormParam("description") String description) {

		Optional<Owner> optionalOwner = ownerRepository.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		Visit visit = new Visit();
		List<OwnerResource.ErrorInfo> errors = new ArrayList<>();

		if (dateStr != null && !dateStr.isBlank()) {
			try {
				visit.setDate(LocalDate.parse(dateStr));
			} catch (Exception e) {
				errors.add(new OwnerResource.ErrorInfo("date", "Invalid date format"));
			}
		}

		if (description == null || description.isBlank()) {
			errors.add(new OwnerResource.ErrorInfo("description", "must not be blank"));
		} else {
			visit.setDescription(description);
		}

		if (!errors.isEmpty()) {
			TemplateInstance template = createOrUpdateVisitForm
				.data("activeMenu", "owners")
				.data("owner", owner)
				.data("pet", pet)
				.data("visit", visit)
				.data("errors", errors);
			return Response.ok(template).build();
		}

		pet.addVisit(visit);
		ownerRepository.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

}
