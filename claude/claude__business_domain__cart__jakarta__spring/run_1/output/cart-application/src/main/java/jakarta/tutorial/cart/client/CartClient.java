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
package jakarta.tutorial.cart.client;

import java.util.Iterator;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import jakarta.tutorial.cart.ejb.Cart;
import jakarta.tutorial.cart.util.BookException;

/**
 *
 * The client class for the CartBean example. Client adds books to the cart,
 * prints the contents of the cart, and then removes a book which hasn't been
 * added yet, causing a BookException.
 * @author ian
 */
@SpringBootApplication
@ComponentScan(basePackages = {"jakarta.tutorial.cart"})
public class CartClient implements CommandLineRunner {

    private final ApplicationContext context;

    public CartClient(ApplicationContext context) {
        this.context = context;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CartClient.class, args);
    }

    @Override
    public void run(String... args) {
        doTest();
    }

    public void doTest() {
        try {
            Cart cart = context.getBean(Cart.class);
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
