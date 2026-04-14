package spring.tutorial.customer.ejb;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import spring.tutorial.customer.data.Customer;

@Named("customerBean")
@ApplicationScoped
public class CustomerBean {

    protected CustomerRestClient client;
    private static final Logger logger
            = Logger.getLogger(CustomerBean.class.getName());

    @PostConstruct
    private void init() {
        try {
            client = RestClientBuilder.newBuilder()
                    .baseUri(URI.create("http://localhost:8080/webapi/Customer"))
                    .build(CustomerRestClient.class);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize REST client", e);
        }
    }

    @PreDestroy
    private void clean() {
        // no-op
    }

    public String createCustomer(Customer customer) {
        if (customer == null) {
            logger.log(Level.WARNING, "customer is null.");
            return "customerError";
        }
        String navigation;
        try {
            Response response = client.createCustomer(customer);
            if (response.getStatus() >= 200 && response.getStatus() < 300 && response.getStatus() == 201) {
                navigation = "customerCreated";
            } else {
                logger.log(Level.WARNING,
                        "couldn''t create customer with id {0}. Status returned was {1}",
                        new Object[]{customer.getId(), response.getStatus()});
                FacesContext context = FacesContext.getCurrentInstance();
                if (context != null) {
                    context.addMessage(null,
                            new FacesMessage("Could not create customer."));
                }
                navigation = "customerError";
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating customer", e);
            navigation = "customerError";
        }
        return navigation;
    }

    public String retrieveCustomer(String id) {
        String navigation;
        try {
            Customer customer = client.getCustomer(id);
            if (customer == null) {
                navigation = "customerError";
            } else {
                navigation = "customerRetrieved";
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving customer", e);
            navigation = "customerError";
        }
        return navigation;
    }

    public List<Customer> retrieveAllCustomers() {
        try {
            return client.getAllCustomers().getCustomers();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving all customers", e);
            return List.of();
        }
    }
}
