package spring.tutorial.customer.ejb;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import spring.tutorial.customer.data.Customer;
import spring.tutorial.customer.data.Customers;

@RegisterRestClient
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CustomerRestClient {

    @GET
    @Path("all")
    Customers getAllCustomers();

    @GET
    @Path("{id}")
    Customer getCustomer(@PathParam("id") String id);

    @POST
    Response createCustomer(Customer customer);
}
