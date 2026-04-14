package spring.tutorial.customer.ejb;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Stateless;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import spring.tutorial.customer.data.Customer;

@Stateless
public class CustomerBean {

    protected Client client;
    protected WebTarget target;
    private static final Logger logger = Logger.getLogger(CustomerBean.class.getName());

    @PostConstruct
    private void init() {
        client = ClientBuilder.newClient();
        target = client.target("http://localhost:8080/webapi/Customer");
    }

    @PreDestroy
    private void clean() {
        if (client != null) {
            client.close();
        }
    }

    public String createCustomer(Customer customer) {
        if (customer == null) {
            logger.log(Level.WARNING, "customer is null.");
            return "customerError";
        }
        String navigation;
        Response response = target.request()
                .post(Entity.entity(customer, MediaType.APPLICATION_JSON));

        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
                && response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            navigation = "customerCreated";
        } else {
            logger.log(Level.WARNING,
                    "couldn''t create customer with id {0}. Status returned was {1}",
                    new Object[]{customer.getId(), response.getStatus()});
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                context.addMessage(null, new FacesMessage("Could not create customer."));
            }
            navigation = "customerError";
        }
        response.close();
        return navigation;
    }

    public String retrieveCustomer(String id) {
        String navigation;
        Response response = target.path("{id}")
                .resolveTemplate("id", id)
                .request(MediaType.APPLICATION_JSON)
                .get();

        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            Customer customer = response.readEntity(Customer.class);
            if (customer == null) {
                navigation = "customerError";
            } else {
                navigation = "customerRetrieved";
            }
        } else {
            navigation = "customerError";
        }
        response.close();
        return navigation;
    }

    public List<Customer> retrieveAllCustomers() {
        Response response = target.path("all")
                .request(MediaType.APPLICATION_JSON)
                .get();

        List<Customer> customers = null;
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            customers = response.readEntity(new GenericType<List<Customer>>() {});
        }
        response.close();
        return customers != null ? customers : List.of();
    }
}
