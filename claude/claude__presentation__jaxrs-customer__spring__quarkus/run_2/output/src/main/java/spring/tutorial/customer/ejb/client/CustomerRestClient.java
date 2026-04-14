package spring.tutorial.customer.ejb.client;

import java.util.List;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import spring.tutorial.customer.data.Customer;
import spring.tutorial.customer.data.CustomerList;

@RegisterRestClient(configKey = "customer-api")
public interface CustomerRestClient {

    @GET
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    CustomerList getAllCustomers();

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Customer getCustomer(@PathParam("id") String id);

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response createCustomer(Customer customer);

    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    Response updateCustomer(@PathParam("id") String id, Customer customer);

    @DELETE
    @Path("/{id}")
    Response deleteCustomer(@PathParam("id") String id);
}
