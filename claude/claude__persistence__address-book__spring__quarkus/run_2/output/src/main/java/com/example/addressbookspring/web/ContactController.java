package com.example.addressbookspring.web;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.example.addressbookspring.entity.Contact;
import com.example.addressbookspring.service.ContactService;

/**
 * REST resource for Contact CRUD operations.
 * Migrated from Spring JSF controller to Quarkus JAX-RS resource.
 */
@Path("/api/contacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactController {

    @Inject
    ContactService contactService;

    @GET
    public List<Contact> listAll() {
        return contactService.findAll();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Contact contact = contactService.find(id);
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(contact).build();
    }

    @POST
    public Response create(@Valid Contact contact) {
        Contact created = contactService.create(contact);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid Contact contact) {
        Contact existing = contactService.find(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        contact.setId(id);
        Contact updated = contactService.edit(contact);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        Contact existing = contactService.find(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        contactService.removeById(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/count")
    public Response count() {
        int count = contactService.count();
        return Response.ok(count).build();
    }
}
