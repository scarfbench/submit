package spring.examples.tutorial.cart;

public class Application {

    public static void main(String[] args) {
        String baseUrl = System.getProperty("app.cart.url", "http://localhost:8080/cart");

        CartClient cartClient = new CartClient(baseUrl);

        try {
            cartClient.doCartOperations();
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
