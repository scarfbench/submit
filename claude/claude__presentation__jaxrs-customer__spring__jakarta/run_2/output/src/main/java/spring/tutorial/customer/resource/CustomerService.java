package spring.tutorial.customer.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
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
import spring.tutorial.customer.data.Address;
import spring.tutorial.customer.data.Customer;

@Path("/Customer")
@RequestScoped
@Transactional
public class CustomerService {

    public static final Logger logger =
            Logger.getLogger(CustomerService.class.getCanonicalName());
    @PersistenceContext(unitName = "customerPU")
    private EntityManager em;
    private CriteriaBuilder cb;

    @PostConstruct
    private void init() {
        cb = em.getCriteriaBuilder();
    }

    @GET
    @Path("all")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Customer> getAllCustomers() {
        List<Customer> customers = null;
        try {
            customers = this.findAllCustomers();
            if (customers == null) {
                return List.of();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error calling findAllCustomers()",
                    new Object[]{ex.getMessage()});
        }
        return customers;
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Customer getCustomer(@PathParam("id") String customerId) {
        Customer customer = null;

        try {
            customer = findById(customerId);
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error calling findCustomer() for customerId {0}. {1}",
                    new Object[]{customerId, ex.getMessage()});
        }
        return customer;
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createCustomer(Customer customer) {

        try {
            long customerId = persist(customer);
            return Response.created(URI.create("/" + customerId)).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "Error creating customer for customerId {0}. {1}",
                    new Object[]{customer.getId(), e.getMessage()});
            return Response.serverError().build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateCustomer(@PathParam("id") String customerId, Customer customer) {

        try {
            Customer oldCustomer = findById(customerId);

            if (oldCustomer == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                persist(customer);
                return Response.status(303).build();
            }
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteCustomer(@PathParam("id") String customerId) {
        try {
            if (!remove(customerId)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.noContent().build();
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error calling deleteCustomer() for customerId {0}. {1}",
                    new Object[]{customerId, ex.getMessage()});
            return Response.serverError().build();
        }
    }

    private long persist(Customer customer) {
        try {
            Address address = customer.getAddress();
            em.persist(address);
            em.persist(customer);
        } catch (Exception ex) {
            logger.warning("Something went wrong when persisting the customer");
        }
        return customer.getId();
    }

    private Customer findById(String customerId) {
        Customer customer = null;
        try {
            customer = em.find(Customer.class, customerId);
            return customer;
        } catch (Exception ex) {
            logger.log(Level.WARNING,
                    "Couldn't fine customer with ID of {0}", customerId);
        }
        return customer;
    }

    @SuppressWarnings("unchecked")
    private List<Customer> findAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try {
            customers = (List<Customer>) em.createNamedQuery("findAllCustomers").getResultList();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error when finding all customers");
        }
        return customers;
    }

    private boolean remove(String customerId) {
        Customer customer;
        try {
            customer = em.find(Customer.class, customerId);
            Address address = customer.getAddress();
            em.remove(address);
            em.remove(customer);
            return true;
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Couldn't remove customer with ID {0}", customerId);
            return false;
        }
    }
}
