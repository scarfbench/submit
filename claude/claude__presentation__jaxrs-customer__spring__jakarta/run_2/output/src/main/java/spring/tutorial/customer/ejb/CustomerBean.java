package spring.tutorial.customer.ejb;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import spring.tutorial.customer.data.Customer;

@Named("customerBean")
@ApplicationScoped
public class CustomerBean {

    protected Client client;
    protected WebTarget target;
    private static final Logger logger
            = Logger.getLogger(CustomerBean.class.getName());

    @PostConstruct
    private void init() {
        client = ClientBuilder.newClient();
        target = client.target("http://localhost:8080/customer/webapi/Customer");
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
        Response response = target
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(customer, MediaType.APPLICATION_JSON));

        if (response.getStatus() == 201) {
            navigation = "customerCreated";
        } else {
            logger.log(Level.WARNING,
                    "couldn''t create customer with id {0}. Status returned was {1}",
                    new Object[]{customer.getId(), response.getStatus()});
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null,
                    new FacesMessage("Could not create customer."));
            navigation = "customerError";
        }
        response.close();
        return navigation;
    }

    public String retrieveCustomer(String id) {
        String navigation;
        Response response = target.path(id)
                .request(MediaType.APPLICATION_JSON)
                .get();

        if (response.getStatus() == 200) {
            Customer customer = response.readEntity(Customer.class);
            navigation = "customerRetrieved";
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

        List<Customer> customers = response.readEntity(new GenericType<List<Customer>>() {});
        response.close();
        return customers;
    }
}
