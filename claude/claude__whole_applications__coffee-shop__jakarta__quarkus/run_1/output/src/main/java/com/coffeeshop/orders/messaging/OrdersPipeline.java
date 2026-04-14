package com.coffeeshop.orders.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class OrdersPipeline {

  @Inject
  @Channel("barista")
  Emitter<String> barista;

  @Inject
  @Channel("kitchen")
  Emitter<String> kitchen;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public void publishToBarista(OrderCommand cmd) {
    try {
      System.out.println("[orders] sending barista command: " + cmd);
      barista.send(MAPPER.writeValueAsString(cmd));
    } catch (Exception e) {
      System.err.println("[orders] failed to publish to barista: " + e);
    }
  }

  public void publishToKitchen(OrderCommand cmd) {
    try {
      System.out.println("[orders] sending kitchen command: " + cmd);
      kitchen.send(MAPPER.writeValueAsString(cmd));
    } catch (Exception e) {
      System.err.println("[orders] failed to publish to kitchen: " + e);
    }
  }

  // simple DTO for messages
  public record OrderCommand(String target, long orderId, String item, int quantity) {}
}
