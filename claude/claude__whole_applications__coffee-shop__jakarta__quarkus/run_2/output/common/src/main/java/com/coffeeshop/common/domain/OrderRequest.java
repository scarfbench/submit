package com.coffeeshop.common.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class OrderRequest {

    @NotBlank
    private String customer;
    @NotBlank
    private String item;
    @Min(1)
    private int quantity;

    public OrderRequest() {
    }

    public OrderRequest(String customer, String item, int quantity) {
        this.customer = customer;
        this.item = item;
        this.quantity = quantity;
    }

    // Record-style accessors for backward compat
    public String customer() {
        return customer;
    }

    public String item() {
        return item;
    }

    public int quantity() {
        return quantity;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
