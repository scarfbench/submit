package org.springframework.samples.petclinic.owner;

import java.net.URI;
import java.util.List;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/owners")
public class OwnerResource {

	@Inject
	OwnerRepository owners;

	@Inject
	Template findOwners;

	@Inject
	Template ownersList;

	@Inject
	Template ownerDetails;

	@Inject
	Template createOrUpdateOwnerForm;

	@GET
	@Path("/find")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initFindForm() {
		return findOwners.data("menu", "owners").data("owner", new Owner());
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Object processFindForm(@QueryParam("lastName") @DefaultValue("") String lastName,
			@QueryParam("page") @DefaultValue("1") int page) {

		int pageSize = 5;
		List<Owner> results = owners.findByLastNameStartingWith(lastName, page, pageSize);
		long totalItems = owners.countByLastNameStartingWith(lastName);

		if (results.isEmpty()) {
			return findOwners.data("menu", "owners")
					.data("owner", new Owner())
					.data("noResults", true);
		}

		if (totalItems == 1) {
			Owner owner = results.get(0);
			return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
		}

		int totalPages = (int) Math.ceil((double) totalItems / pageSize);

		return ownersList.data("menu", "owners")
				.data("listOwners", results)
				.data("currentPage", page)
				.data("totalPages", totalPages)
				.data("totalItems", totalItems);
	}

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm() {
		return createOrUpdateOwnerForm.data("menu", "owners")
				.data("owner", new Owner())
				.data("isNew", true);
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processCreationForm(
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("address") String address,
			@FormParam("city") String city,
			@FormParam("telephone") String telephone) {

		Owner owner = new Owner();
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);

		// Basic validation
		if (firstName == null || firstName.isBlank()
				|| lastName == null || lastName.isBlank()
				|| address == null || address.isBlank()
				|| city == null || city.isBlank()
				|| telephone == null || !telephone.matches("\\d{10}")) {
			return createOrUpdateOwnerForm.data("menu", "owners")
					.data("owner", owner)
					.data("isNew", true)
					.data("error", "Please fill all fields correctly. Telephone must be 10 digits.");
		}

		owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
	}

	@GET
	@Path("/{ownerId: [0-9]+}")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance showOwner(@PathParam("ownerId") int ownerId) {
		Owner owner = owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		return ownerDetails.data("menu", "owners").data("owner", owner);
	}

	@GET
	@Path("/{ownerId: [0-9]+}/edit")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initUpdateOwnerForm(@PathParam("ownerId") int ownerId) {
		Owner owner = owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		return createOrUpdateOwnerForm.data("menu", "owners")
				.data("owner", owner)
				.data("isNew", false);
	}

	@POST
	@Path("/{ownerId: [0-9]+}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processUpdateOwnerForm(
			@PathParam("ownerId") int ownerId,
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("address") String address,
			@FormParam("city") String city,
			@FormParam("telephone") String telephone) {

		Owner owner = owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);

		// Basic validation
		if (firstName == null || firstName.isBlank()
				|| lastName == null || lastName.isBlank()
				|| address == null || address.isBlank()
				|| city == null || city.isBlank()
				|| telephone == null || !telephone.matches("\\d{10}")) {
			return createOrUpdateOwnerForm.data("menu", "owners")
					.data("owner", owner)
					.data("isNew", false)
					.data("error", "Please fill all fields correctly. Telephone must be 10 digits.");
		}

		owner.setId(ownerId);
		owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

}
