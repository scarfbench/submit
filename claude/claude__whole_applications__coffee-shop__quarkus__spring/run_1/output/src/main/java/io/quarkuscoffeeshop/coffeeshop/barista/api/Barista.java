package io.quarkuscoffeeshop.coffeeshop.barista.api;

/**
 * Barista interface for handling order messages
 */
public interface Barista {

    void onOrderIn(final String orderInJson);

    void onRemakeIn(final String remakeJson);

    void onCancelOrder(final String cancellationJson);
}
