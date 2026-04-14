package com.coffeeshop.common.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OrderRequest {

    @NotNull @Size(min = 1)
    private String customer;

    @NotNull @Size(min = 1)
    private String item;

    @Min(1)
    private int quantity;

    public OrderRequest() {}

    public OrderRequest(String customer, String item, int quantity) {
        this.customer = customer;
        this.item = item;
        this.quantity = quantity;
    }

    public String customer() { return customer; }
    public String item() { return item; }
    public int quantity() { return quantity; }

    // Jackson needs standard getters/setters
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
