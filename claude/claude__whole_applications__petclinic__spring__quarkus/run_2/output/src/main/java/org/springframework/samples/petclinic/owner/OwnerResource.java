package org.springframework.samples.petclinic.owner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
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
	OwnerRepository ownerRepository;

	@Inject
	Validator validator;

	@Inject
	@Location("owners/findOwners.html")
	Template findOwners;

	@Inject
	@Location("owners/ownersList.html")
	Template ownersList;

	@Inject
	@Location("owners/ownerDetails.html")
	Template ownerDetails;

	@Inject
	@Location("owners/createOrUpdateOwnerForm.html")
	Template createOrUpdateOwnerForm;

	@GET
	@Path("/find")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initFindForm() {
		return findOwners.data("activeMenu", "owners");
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response processFindForm(
			@QueryParam("page") Integer page,
			@QueryParam("lastName") String lastName) {

		if (page == null) page = 1;
		if (lastName == null) lastName = "";

		int pageSize = 5;
		long totalItems = ownerRepository.countByLastNameStartingWith(lastName);
		List<Owner> owners = ownerRepository.findByLastNameStartingWith(lastName, page, pageSize);

		if (totalItems == 0) {
			TemplateInstance template = findOwners
				.data("activeMenu", "owners")
				.data("error", "has not been found")
				.data("lastName", lastName);
			return Response.ok(template).build();
		}

		if (totalItems == 1) {
			Owner owner = owners.get(0);
			return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
		}

		int totalPages = (int) Math.ceil((double) totalItems / pageSize);
		List<Integer> pagesList = new ArrayList<>();
		for (int i = 1; i <= totalPages; i++) {
			pagesList.add(i);
		}

		TemplateInstance template = ownersList
			.data("activeMenu", "owners")
			.data("listOwners", owners)
			.data("currentPage", page)
			.data("totalPages", totalPages)
			.data("totalItems", totalItems)
			.data("pages", pagesList)
			.data("lastName", lastName);
		return Response.ok(template).build();
	}

	@GET
	@Path("/new")
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance initCreationForm() {
		Owner owner = new Owner();
		return createOrUpdateOwnerForm
			.data("activeMenu", "owners")
			.data("owner", owner)
			.data("isNew", true);
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
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

		var violations = validator.validate(owner);
		if (!violations.isEmpty()) {
			List<ErrorInfo> errors = new ArrayList<>();
			for (ConstraintViolation<Owner> v : violations) {
				errors.add(new ErrorInfo(v.getPropertyPath().toString(), v.getMessage()));
			}
			TemplateInstance template = createOrUpdateOwnerForm
				.data("activeMenu", "owners")
				.data("owner", owner)
				.data("isNew", true)
				.data("errors", errors);
			return Response.ok(template).build();
		}

		ownerRepository.save(owner);
		return Response.seeOther(URI.create("/owners/" + owner.getId())).build();
	}

	@GET
	@Path("/{ownerId}")
	@Produces(MediaType.TEXT_HTML)
	public Response showOwner(@PathParam("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = ownerRepository.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		TemplateInstance template = ownerDetails
			.data("activeMenu", "owners")
			.data("owner", owner);
		return Response.ok(template).build();
	}

	@GET
	@Path("/{ownerId}/edit")
	@Produces(MediaType.TEXT_HTML)
	public Response initUpdateOwnerForm(@PathParam("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = ownerRepository.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		Owner owner = optionalOwner.get();
		TemplateInstance template = createOrUpdateOwnerForm
			.data("activeMenu", "owners")
			.data("owner", owner)
			.data("isNew", false);
		return Response.ok(template).build();
	}

	@POST
	@Path("/{ownerId}/edit")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	@Transactional
	public Response processUpdateOwnerForm(
			@PathParam("ownerId") int ownerId,
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("address") String address,
			@FormParam("city") String city,
			@FormParam("telephone") String telephone) {

		Optional<Owner> optionalOwner = ownerRepository.findById(ownerId);
		if (optionalOwner.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		Owner owner = optionalOwner.get();
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);

		var violations = validator.validate(owner);
		if (!violations.isEmpty()) {
			List<ErrorInfo> errors = new ArrayList<>();
			for (ConstraintViolation<Owner> v : violations) {
				errors.add(new ErrorInfo(v.getPropertyPath().toString(), v.getMessage()));
			}
			TemplateInstance template = createOrUpdateOwnerForm
				.data("activeMenu", "owners")
				.data("owner", owner)
				.data("isNew", false)
				.data("errors", errors);
			return Response.ok(template).build();
		}

		ownerRepository.save(owner);
		return Response.seeOther(URI.create("/owners/" + ownerId)).build();
	}

	public static class ErrorInfo {
		public final String field;
		public final String message;

		public ErrorInfo(String field, String message) {
			this.field = field;
			this.message = message;
		}

		public String getField() {
			return field;
		}

		public String getMessage() {
			return message;
		}
	}

}
