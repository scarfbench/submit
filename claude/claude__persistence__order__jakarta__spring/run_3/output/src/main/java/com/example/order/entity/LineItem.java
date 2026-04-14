package com.example.order.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@IdClass(LineItemKey.class)
@Entity
@Table(name = "PERSISTENCE_ORDER_LINEITEM")
public class LineItem implements Serializable {
    private static final long serialVersionUID = 3229188813505619743L;
    private int itemId;
    private int quantity;
    private VendorPart vendorPart;
    private CustomerOrder customerOrder;

    public LineItem() {
    }

    public LineItem(CustomerOrder order, int quantity, VendorPart vendorPart) {
        this.customerOrder = order;
        this.itemId = order.getNextId();
        this.quantity = quantity;
        this.vendorPart = vendorPart;
    }

    @Id
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @JoinColumn(name = "VENDORPARTNUMBER")
    @ManyToOne
    public VendorPart getVendorPart() {
        return vendorPart;
    }

    public void setVendorPart(VendorPart vendorPart) {
        this.vendorPart = vendorPart;
    }

    @JsonIgnore
    @Id
    @ManyToOne
    @JoinColumn(name = "ORDERID")
    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }
}
