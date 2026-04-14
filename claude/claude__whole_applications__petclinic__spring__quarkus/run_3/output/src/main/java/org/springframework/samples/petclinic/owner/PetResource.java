package org.springframework.samples.petclinic.owner;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collection;

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
public class PetResource {

	@Inject
	OwnerRepository owners;

	@Inject
	PetTypeRepository types;

	@Inject
	Template createOrUpdatePetForm;

	@GET
	@Path("/{ownerId: [0-9]+}/pets/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm(@PathParam("ownerId") int ownerId) {
		Owner owner = owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		Collection<PetType> petTypes = types.findPetTypes();
		Pet pet = new Pet();
		return createOrUpdatePetForm.data("menu", "owners")
				.data("owner", owner)
				.data("pet", pet)
				.data("types", petTypes)
				.data("isNew", true);
	}

	@POST
	@Path("/{ownerId: [0-9]+}/pets/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processCreationForm(
			@PathParam("ownerId") int ownerId,
			@FormParam("name") String name,
			@FormParam("birthDate") String birthDateStr,
			@FormParam("type") String typeName) {

		Owner owner = owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		Pet pet = new Pet();
		pet.setName(name);

		if (birthDateStr != null && !birthDateStr.isBlank()) {
			pet.setBirthDate(LocalDate.parse(birthDateStr));
		}

		if (typeName != null && !typeName.isBlank()) {
			PetType petType = types.findByName(typeName);
			pet.setType(petType);
		}

		// Validation
		Collection<PetType> petTypes = types.findPetTypes();
		if (name == null || name.isBlank() || pet.getType() == null || pet.getBirthDate() == null) {
			return createOrUpdatePetForm.data("menu", "owners")
					.data("owner", owner)
					.data("pet", pet)
					.data("types", petTypes)
					.data("isNew", true)
					.data("error", "Please fill all required fields.");
		}

		owner.addPet(pet);
		owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	@GET
	@Path("/{ownerId: [0-9]+}/pets/{petId: [0-9]+}/edit")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initUpdateForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId) {
		Owner owner = owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		Pet pet = owner.getPet(petId);
		Collection<PetType> petTypes = types.findPetTypes();
		return createOrUpdatePetForm.data("menu", "owners")
				.data("owner", owner)
				.data("pet", pet)
				.data("types", petTypes)
				.data("isNew", false);
	}

	@POST
	@Path("/{ownerId: [0-9]+}/pets/{petId: [0-9]+}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Object processUpdateForm(
			@PathParam("ownerId") int ownerId,
			@PathParam("petId") int petId,
			@FormParam("name") String name,
			@FormParam("birthDate") String birthDateStr,
			@FormParam("type") String typeName) {

		Owner owner = owners.findById(ownerId)
				.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));

		Pet existingPet = owner.getPet(petId);

		if (existingPet != null) {
			existingPet.setName(name);
			if (birthDateStr != null && !birthDateStr.isBlank()) {
				existingPet.setBirthDate(LocalDate.parse(birthDateStr));
			}
			if (typeName != null && !typeName.isBlank()) {
				PetType petType = types.findByName(typeName);
				existingPet.setType(petType);
			}
		}

		owners.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

}
