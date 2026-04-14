/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package jakarta.tutorial.addressbook.resource;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.tutorial.addressbook.entity.Contact;

/**
 * RESTful resource for Contact CRUD operations.
 * Replaces the JSF ContactController and EJB ContactFacade from the Jakarta EE version.
 */
@Path("/api/contacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {

    /**
     * List all contacts with optional pagination.
     */
    @GET
    public List<Contact> list(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return Contact.findAll().page(page, size).list();
    }

    /**
     * Get total count of contacts.
     */
    @GET
    @Path("/count")
    public long count() {
        return Contact.count();
    }

    /**
     * Get a single contact by ID.
     */
    @GET
    @Path("/{id}")
    public Contact get(@PathParam("id") Long id) {
        Contact contact = Contact.findById(id);
        if (contact == null) {
            throw new WebApplicationException("Contact with id " + id + " not found", Response.Status.NOT_FOUND);
        }
        return contact;
    }

    /**
     * Create a new contact.
     */
    @POST
    @Transactional
    public Response create(@Valid Contact contact) {
        contact.id = null; // ensure new entity
        contact.persist();
        return Response.status(Response.Status.CREATED).entity(contact).build();
    }

    /**
     * Update an existing contact.
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public Contact update(@PathParam("id") Long id, @Valid Contact updatedContact) {
        Contact existing = Contact.findById(id);
        if (existing == null) {
            throw new WebApplicationException("Contact with id " + id + " not found", Response.Status.NOT_FOUND);
        }
        existing.firstName = updatedContact.firstName;
        existing.lastName = updatedContact.lastName;
        existing.email = updatedContact.email;
        existing.mobilePhone = updatedContact.mobilePhone;
        existing.homePhone = updatedContact.homePhone;
        existing.birthday = updatedContact.birthday;
        return existing;
    }

    /**
     * Delete a contact by ID.
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Contact contact = Contact.findById(id);
        if (contact == null) {
            throw new WebApplicationException("Contact with id " + id + " not found", Response.Status.NOT_FOUND);
        }
        contact.delete();
        return Response.noContent().build();
    }
}
