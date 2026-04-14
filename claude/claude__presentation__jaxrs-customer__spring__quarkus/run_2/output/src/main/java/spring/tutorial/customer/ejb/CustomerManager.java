package spring.tutorial.customer.ejb;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import spring.tutorial.customer.data.Customer;

@Named("customerManager")
@RequestScoped
public class CustomerManager implements Serializable {
    private Customer customer;
    private List<Customer> customers;
    private static final Logger logger = Logger.getLogger(CustomerManager.class.getName());

    @Inject
    CustomerBean customerBean;

    @PostConstruct
    private void init() {
        logger.info("new customer created");
        customer = new Customer();
        setCustomers(customerBean.retrieveAllCustomers());
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Customer> getCustomers() {
        return customerBean.retrieveAllCustomers();
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
