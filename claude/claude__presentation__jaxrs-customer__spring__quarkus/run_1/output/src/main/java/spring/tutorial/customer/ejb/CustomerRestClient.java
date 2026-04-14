package spring.tutorial.customer.ejb;

import java.util.List;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import spring.tutorial.customer.data.Customer;

@Path("/Customer")
@RegisterRestClient(configKey = "spring.tutorial.customer.ejb.CustomerRestClient")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CustomerRestClient {

    @GET
    @Path("/all")
    List<Customer> getAllCustomers();

    @GET
    @Path("/{id}")
    Customer getCustomer(@PathParam("id") String id);

    @POST
    Response createCustomer(Customer customer);

    @PUT
    @Path("/{id}")
    Response updateCustomer(@PathParam("id") String id, Customer customer);

    @DELETE
    @Path("/{id}")
    Response deleteCustomer(@PathParam("id") String id);
}
