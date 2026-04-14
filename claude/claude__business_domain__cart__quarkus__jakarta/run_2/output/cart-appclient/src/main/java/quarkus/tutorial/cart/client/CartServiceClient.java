package quarkus.tutorial.cart.client;

import quarkus.tutorial.cart.common.BookException;

import java.util.List;

public interface CartServiceClient {

    void initialize(String name, String id) throws BookException;

    void addBook(String title);

    List<String> getContents();

    void removeBook(String title) throws BookException;

    void clearCart();
}
