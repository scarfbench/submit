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

@Path("/owners/{ownerId}/pets")
public class PetResource {

	@Inject
	OwnerRepository ownerRepository;

	@Inject
	PetTypeRepository petTypeRepository;

	@Inject
	@Location("pets/createOrUpdatePetForm.html")
	Template createOrUpdatePetForm;

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public Response initCreationForm(@PathParam("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = ownerRepository.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		Pet pet = new Pet();
		List<PetType> types = petTypeRepository.findPetTypes();

		TemplateInstance template = createOrUpdatePetForm
			.data("activeMenu", "owners")
			.data("owner", owner)
			.data("pet", pet)
			.data("types", types)
			.data("isNew", true);
		return Response.ok(template).build();
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Response processCreationForm(
			@PathParam("ownerId") int ownerId,
			@FormParam("name") String name,
			@FormParam("birthDate") String birthDateStr,
			@FormParam("typeId") Integer typeId) {

		Optional<Owner> optionalOwner = ownerRepository.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		List<PetType> types = petTypeRepository.findPetTypes();

		Pet pet = new Pet();
		pet.setName(name);

		List<OwnerResource.ErrorInfo> errors = new ArrayList<>();

		if (name == null || name.isBlank()) {
			errors.add(new OwnerResource.ErrorInfo("name", "is required"));
		}

		if (birthDateStr != null && !birthDateStr.isBlank()) {
			try {
				LocalDate birthDate = LocalDate.parse(birthDateStr);
				pet.setBirthDate(birthDate);
				if (birthDate.isAfter(LocalDate.now())) {
					errors.add(new OwnerResource.ErrorInfo("birthDate", "Birth date cannot be in the future"));
				}
			} catch (Exception e) {
				errors.add(new OwnerResource.ErrorInfo("birthDate", "Invalid date format"));
			}
		} else {
			errors.add(new OwnerResource.ErrorInfo("birthDate", "is required"));
		}

		if (typeId != null) {
			PetType type = petTypeRepository.findById(typeId);
			if (type != null) {
				pet.setType(type);
			} else {
				errors.add(new OwnerResource.ErrorInfo("type", "is required"));
			}
		} else {
			errors.add(new OwnerResource.ErrorInfo("type", "is required"));
		}

		if (name != null && !name.isBlank() && owner.getPet(name, true) != null) {
			errors.add(new OwnerResource.ErrorInfo("name", "already exists"));
		}

		if (!errors.isEmpty()) {
			TemplateInstance template = createOrUpdatePetForm
				.data("activeMenu", "owners")
				.data("owner", owner)
				.data("pet", pet)
				.data("types", types)
				.data("isNew", true)
				.data("errors", errors);
			return Response.ok(template).build();
		}

		owner.addPet(pet);
		ownerRepository.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	@GET
	@Path("/{petId}/edit")
	@Produces(MediaType.TEXT_HTML)
	public Response initUpdateForm(
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
		List<PetType> types = petTypeRepository.findPetTypes();

		TemplateInstance template = createOrUpdatePetForm
			.data("activeMenu", "owners")
			.data("owner", owner)
			.data("pet", pet)
			.data("types", types)
			.data("isNew", false);
		return Response.ok(template).build();
	}

	@POST
	@Path("/{petId}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Response processUpdateForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId,
			@FormParam("name") String name,
			@FormParam("birthDate") String birthDateStr,
			@FormParam("typeId") Integer typeId) {

		Optional<Owner> optionalOwner = ownerRepository.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		Pet existingPet = owner.getPet(petId);
		if (existingPet == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		List<PetType> types = petTypeRepository.findPetTypes();

		List<OwnerResource.ErrorInfo> errors = new ArrayList<>();

		if (name == null || name.isBlank()) {
			errors.add(new OwnerResource.ErrorInfo("name", "is required"));
		}

		LocalDate birthDate = null;
		if (birthDateStr != null && !birthDateStr.isBlank()) {
			try {
				birthDate = LocalDate.parse(birthDateStr);
				if (birthDate.isAfter(LocalDate.now())) {
					errors.add(new OwnerResource.ErrorInfo("birthDate", "Birth date cannot be in the future"));
				}
			} catch (Exception e) {
				errors.add(new OwnerResource.ErrorInfo("birthDate", "Invalid date format"));
			}
		} else {
			errors.add(new OwnerResource.ErrorInfo("birthDate", "is required"));
		}

		PetType type = null;
		if (typeId != null) {
			type = petTypeRepository.findById(typeId);
		}

		if (!errors.isEmpty()) {
			TemplateInstance template = createOrUpdatePetForm
				.data("activeMenu", "owners")
				.data("owner", owner)
				.data("pet", existingPet)
				.data("types", types)
				.data("isNew", false)
				.data("errors", errors);
			return Response.ok(template).build();
		}

		existingPet.setName(name);
		existingPet.setBirthDate(birthDate);
		if (type != null) {
			existingPet.setType(type);
		}
		ownerRepository.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

}
