package com.coffeeshop.orders.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.Optional;

@ApplicationScoped
public class OrdersPipeline {

  @Channel("barista")
  Emitter<String> barista;

  @Channel("kitchen")
  Emitter<String> kitchen;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public void publishToBarista(OrderCommand cmd) {
    try {
      System.out.println("[orders] sending barista command: " + cmd);
      if (barista != null) {
        barista.send(toJson(cmd));
      }
    } catch (Exception e) {
      System.err.println("[orders] failed to send barista command (Kafka may be unavailable): " + e.getMessage());
    }
  }

  public void publishToKitchen(OrderCommand cmd) {
    try {
      System.out.println("[orders] sending kitchen command: " + cmd);
      if (kitchen != null) {
        kitchen.send(toJson(cmd));
      }
    } catch (Exception e) {
      System.err.println("[orders] failed to send kitchen command (Kafka may be unavailable): " + e.getMessage());
    }
  }

  private String toJson(Object obj) {
    try {
      return MAPPER.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  // simple DTO for messages
  public record OrderCommand(String target, long orderId, String item, int quantity) {}
}
