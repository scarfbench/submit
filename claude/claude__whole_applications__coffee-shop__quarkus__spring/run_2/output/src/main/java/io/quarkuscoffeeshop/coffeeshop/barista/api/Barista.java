package io.quarkuscoffeeshop.coffeeshop.barista.api;

public interface Barista {

    /**
     * JSON payload corresponding to OrderIn value object
     *
     * @param orderInJson OrderIn
     * @see io.quarkuscoffeeshop.coffeeshop.domain.valueobjects.OrderIn
     */
    void onOrderIn(String orderInJson);
}
