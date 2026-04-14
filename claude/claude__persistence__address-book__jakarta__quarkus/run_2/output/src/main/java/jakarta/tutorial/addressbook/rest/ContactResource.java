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
package jakarta.tutorial.addressbook.rest;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
 * REST resource for Contact CRUD operations.
 * Replaces the original JSF ContactController and EJB ContactFacade.
 */
@Path("/contacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {

    /**
     * List all contacts, with optional pagination.
     */
    @GET
    public List<Contact> listAll(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size) {
        if (page != null && size != null) {
            return Contact.findAll()
                    .page(page, size)
                    .list();
        }
        return Contact.listAll();
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
    public Contact update(@PathParam("id") Long id, @Valid Contact contact) {
        Contact existing = Contact.findById(id);
        if (existing == null) {
            throw new WebApplicationException("Contact with id " + id + " not found", Response.Status.NOT_FOUND);
        }
        existing.firstName = contact.firstName;
        existing.lastName = contact.lastName;
        existing.email = contact.email;
        existing.mobilePhone = contact.mobilePhone;
        existing.homePhone = contact.homePhone;
        existing.birthday = contact.birthday;
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
