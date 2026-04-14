package com.example.addressbook.rest;

import com.example.addressbook.entity.Contact;
import com.example.addressbook.service.ContactService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/contacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {

    @Inject
    private ContactService contactService;

    @GET
    public List<Contact> getAll() {
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
        return Response.ok(contact).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        Contact contact = contactService.find(id);
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        contactService.remove(contact);
        return Response.noContent().build();
    }

    @GET
    @Path("/count")
    public Response count() {
        int count = contactService.count();
        return Response.ok("{\"count\":" + count + "}").build();
    }
}
