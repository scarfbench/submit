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
package com.example.cart.client;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.cart.service.Cart;
import com.example.cart.exception.BookException;

/**
 *
 * The client class for the CartBean example. Client adds books to the cart,
 * prints the contents of the cart, and then removes a book which hasn't been
 * added yet, causing a BookException.
 * @author ian
 */
@Component
public class CartClient {

    @Autowired
    private Cart cart;

    public CartClient() {
    }

    public void doTest() {
        try {
            cart.initialize("Duke d'Url", "123");
            cart.addBook("Infinite Jest");
            cart.addBook("Bel Canto");
            cart.addBook("Kafka on the Shore");

            List<String> bookList = cart.getContents();

            Iterator<String> iterator = bookList.iterator();

            while (iterator.hasNext()) {
                String title = iterator.next();
                System.out.println("Retrieving book title from cart: " + title);
            }

            System.out.println("Removing \"Gravity's Rainbow\" from cart.");
            cart.removeBook("Gravity's Rainbow");
            cart.remove();

            System.exit(0);
        } catch (BookException ex) {
            System.err.println("Caught a BookException: " + ex.getMessage());
            System.exit(0);
        }
    }
}
