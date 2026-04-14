package org.springframework.samples.petclinic.owner;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

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
	PetTypeRepository petTypes;

	@Inject
	Template createOrUpdateOwnerForm;

	@Inject
	Template findOwners;

	@Inject
	Template ownersList;

	@Inject
	Template ownerDetails;

	@Inject
	Template createOrUpdatePetForm;

	@Inject
	Template createOrUpdateVisitForm;

	// ==================== Owner Endpoints ====================

	@GET
	@Path("/find")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initFindForm() {
		return findOwners.data("owner", new Owner()).data("menu", "owners");
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response processFindForm(
			@QueryParam("page") @DefaultValue("1") int page,
			@QueryParam("lastName") @DefaultValue("") String lastName) {

		List<Owner> results = owners.findByLastNameStartingWith(lastName, page, 5);
		long totalItems = owners.countByLastNameStartingWith(lastName);

		if (results.isEmpty()) {
			return Response.ok(
				findOwners.data("owner", new Owner())
					.data("noResults", true)
					.data("menu", "owners")
			).build();
		}

		if (totalItems == 1) {
			Owner owner = results.get(0);
			return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
		}

		int totalPages = (int) Math.ceil((double) totalItems / 5);
		return Response.ok(
			ownersList.data("listOwners", results)
				.data("currentPage", page)
				.data("totalPages", totalPages)
				.data("totalItems", totalItems)
				.data("menu", "owners")
		).build();
	}

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm() {
		return createOrUpdateOwnerForm.data("owner", new Owner())
			.data("isNew", true)
			.data("menu", "owners");
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response processCreationForm(
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

		String errors = validateOwner(owner);
		if (errors != null) {
			return Response.ok(
				createOrUpdateOwnerForm.data("owner", owner)
					.data("isNew", true)
					.data("errors", errors)
					.data("menu", "owners")
			).build();
		}

		owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
	}

	@GET
	@Path("/{ownerId}")
	@Produces(MediaType.TEXT_HTML)
	public Response showOwner(@PathParam("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		return Response.ok(
			ownerDetails.data("owner", owner).data("menu", "owners")
		).build();
	}

	@GET
	@Path("/{ownerId}/edit")
	@Produces(MediaType.TEXT_HTML)
	public Response initUpdateOwnerForm(@PathParam("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		return Response.ok(
			createOrUpdateOwnerForm.data("owner", owner)
				.data("isNew", false)
				.data("menu", "owners")
		).build();
	}

	@POST
	@Path("/{ownerId}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response processUpdateOwnerForm(
			@PathParam("ownerId") int ownerId,
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("address") String address,
			@FormParam("city") String city,
			@FormParam("telephone") String telephone) {

		Optional<Owner> optionalOwner = owners.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);

		String errors = validateOwner(owner);
		if (errors != null) {
			return Response.ok(
				createOrUpdateOwnerForm.data("owner", owner)
					.data("isNew", false)
					.data("errors", errors)
					.data("menu", "owners")
			).build();
		}

		owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	// ==================== Pet Endpoints ====================

	@GET
	@Path("/{ownerId}/pets/new")
	@Produces(MediaType.TEXT_HTML)
	public Response initPetCreationForm(@PathParam("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		List<PetType> types = petTypes.findPetTypes();
		return Response.ok(
			createOrUpdatePetForm.data("owner", owner)
				.data("pet", new Pet())
				.data("types", types)
				.data("isNew", true)
				.data("menu", "owners")
		).build();
	}

	@POST
	@Path("/{ownerId}/pets/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response processPetCreationForm(
			@PathParam("ownerId") int ownerId,
			@FormParam("name") String name,
			@FormParam("birthDate") String birthDate,
			@FormParam("type") String typeId) {

		Optional<Owner> optionalOwner = owners.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();

		Pet pet = new Pet();
		pet.setName(name);
		parseBirthDate(pet, birthDate);
		parsePetType(pet, typeId);

		owner.addPet(pet);
		owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	@GET
	@Path("/{ownerId}/pets/{petId}/edit")
	@Produces(MediaType.TEXT_HTML)
	public Response initPetUpdateForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId) {

		Optional<Owner> optionalOwner = owners.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		List<PetType> types = petTypes.findPetTypes();
		return Response.ok(
			createOrUpdatePetForm.data("owner", owner)
				.data("pet", pet)
				.data("types", types)
				.data("isNew", false)
				.data("menu", "owners")
		).build();
	}

	@POST
	@Path("/{ownerId}/pets/{petId}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response processPetUpdateForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId,
			@FormParam("name") String name,
			@FormParam("birthDate") String birthDate,
			@FormParam("type") String typeId) {

		Optional<Owner> optionalOwner = owners.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		Pet existingPet = owner.getPet(petId);
		if (existingPet != null) {
			existingPet.setName(name);
			parseBirthDate(existingPet, birthDate);
			parsePetType(existingPet, typeId);
		}
		owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	// ==================== Visit Endpoints ====================

	@GET
	@Path("/{ownerId}/pets/{petId}/visits/new")
	@Produces(MediaType.TEXT_HTML)
	public Response initNewVisitForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId) {

		Optional<Owner> optionalOwner = owners.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(
			createOrUpdateVisitForm.data("owner", owner)
				.data("pet", pet)
				.data("visit", new Visit())
				.data("menu", "owners")
		).build();
	}

	@POST
	@Path("/{ownerId}/pets/{petId}/visits/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Transactional
	public Response processNewVisitForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId,
			@FormParam("date") String date,
			@FormParam("description") String description) {

		Optional<Owner> optionalOwner = owners.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		Visit visit = new Visit();
		if (date != null && !date.isBlank()) {
			try {
				visit.setDate(LocalDate.parse(date));
			} catch (DateTimeParseException e) {
				// use default date
			}
		}
		visit.setDescription(description != null ? description : "");
		pet.addVisit(visit);
		owners.save(owner);

		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	// ==================== Helper Methods ====================

	private String validateOwner(Owner owner) {
		StringBuilder errors = new StringBuilder();
		if (owner.getFirstName() == null || owner.getFirstName().isBlank()) {
			errors.append("First name is required. ");
		}
		if (owner.getLastName() == null || owner.getLastName().isBlank()) {
			errors.append("Last name is required. ");
		}
		if (owner.getAddress() == null || owner.getAddress().isBlank()) {
			errors.append("Address is required. ");
		}
		if (owner.getCity() == null || owner.getCity().isBlank()) {
			errors.append("City is required. ");
		}
		if (owner.getTelephone() == null || !owner.getTelephone().matches("\\d{10}")) {
			errors.append("Telephone must be a 10-digit number. ");
		}
		return errors.length() > 0 ? errors.toString() : null;
	}

	private void parseBirthDate(Pet pet, String birthDate) {
		if (birthDate != null && !birthDate.isBlank()) {
			try {
				pet.setBirthDate(LocalDate.parse(birthDate));
			} catch (DateTimeParseException e) {
				// ignore
			}
		}
	}

	private void parsePetType(Pet pet, String typeId) {
		if (typeId != null && !typeId.isBlank()) {
			try {
				PetType type = petTypes.findById(Integer.parseInt(typeId));
				if (type != null) {
					pet.setType(type);
				}
			} catch (NumberFormatException e) {
				// ignore
			}
		}
	}

}
