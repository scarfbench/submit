package io.quarkuscoffeeshop.coffeeshop.barista.api;

public interface Barista {
    public void onOrderIn(String orderInJson);
    public void onRemakeIn(String remakeJson);
    public void onCancelOrder(String cancellationJson);
}