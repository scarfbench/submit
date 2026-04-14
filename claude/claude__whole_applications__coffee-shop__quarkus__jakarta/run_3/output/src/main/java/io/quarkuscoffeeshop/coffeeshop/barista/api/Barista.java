package io.quarkuscoffeeshop.coffeeshop.barista.api;

public interface Barista {
    public void onOrderIn(final String orderInJson);
    public void onRemakeIn(final String remakeJson);
    public void onCancelOrder(final String cancellationJson);
}
