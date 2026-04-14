package com.example.addressbookspring.resource;

import com.example.addressbookspring.entity.Contact;
import com.example.addressbookspring.service.ContactService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
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
        contactService.create(contact);
        return Response.created(URI.create("/api/contacts/" + contact.id))
                .entity(contact)
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, @Valid Contact contact) {
        Contact existing = contactService.find(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        contact.id = id;
        contactService.edit(contact);
        return Response.ok(contact).build();
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

    @GET
    @Path("/count")
    public Response count() {
        return Response.ok(new CountResponse(contactService.count())).build();
    }

    public static class CountResponse {
        public int count;

        public CountResponse() {
        }

        public CountResponse(int count) {
            this.count = count;
        }
    }
}
