package com.example.addressbookspring.rest;

import com.example.addressbookspring.entity.Contact;
import com.example.addressbookspring.service.ContactService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/contacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {

    @Inject
    ContactService contactService;

    @GET
    public List<Contact> listAll() {
        return contactService.findAll();
    }

    @GET
    @Path("/count")
    public int count() {
        return contactService.count();
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
    public Response create(Contact contact) {
        contactService.create(contact);
        return Response.status(Response.Status.CREATED).entity(contact).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Contact contact) {
        Contact existing = contactService.find(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        contact.setId(id);
        contactService.edit(contact);
        Contact updated = contactService.find(id);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        Contact existing = contactService.find(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        contactService.remove(existing);
        return Response.noContent().build();
    }
}
