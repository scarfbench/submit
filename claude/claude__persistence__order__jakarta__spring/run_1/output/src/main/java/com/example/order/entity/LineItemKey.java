/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.example.order.entity;

import java.io.Serializable;
import java.util.Objects;

public class LineItemKey implements Serializable {
    private static final long serialVersionUID = 1562260205094677677L;
    private Integer customerOrder;
    private int itemId;

    public LineItemKey() {}

    public LineItemKey(Integer order, int itemId) {
        this.customerOrder = order;
        this.itemId = itemId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerOrder, itemId);
    }

    @Override
    public boolean equals(Object otherOb) {
        if (this == otherOb) {
            return true;
        }
        if (!(otherOb instanceof LineItemKey other)) {
            return false;
        }
        return Objects.equals(this.customerOrder, other.customerOrder)
                && this.itemId == other.itemId;
    }

    @Override
    public String toString() {
        return customerOrder + "-" + itemId;
    }

    public Integer getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(Integer order) {
        this.customerOrder = order;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
