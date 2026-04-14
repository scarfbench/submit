package jakarta.tutorial.addressbook.rest;

import java.util.List;

import jakarta.ejb.EJB;
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
import jakarta.tutorial.addressbook.ejb.ContactFacade;
import jakarta.tutorial.addressbook.entity.Contact;

/**
 * REST resource for Contact CRUD operations.
 * Provides endpoints for smoke testing.
 */
@Path("/contacts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContactResource {

    @EJB
    private ContactFacade contactFacade;

    @GET
    public List<Contact> getAll() {
        return contactFacade.findAll();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Contact contact = contactFacade.find(id);
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(contact).build();
    }

    @POST
    public Response create(Contact contact) {
        contactFacade.create(contact);
        return Response.status(Response.Status.CREATED).entity(contact).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Long id, Contact contact) {
        Contact existing = contactFacade.find(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        contact.setId(id);
        contactFacade.edit(contact);
        return Response.ok(contact).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        Contact existing = contactFacade.find(id);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        contactFacade.remove(existing);
        return Response.noContent().build();
    }

    @GET
    @Path("/count")
    public Response count() {
        int count = contactFacade.count();
        return Response.ok("{\"count\":" + count + "}").build();
    }
}
