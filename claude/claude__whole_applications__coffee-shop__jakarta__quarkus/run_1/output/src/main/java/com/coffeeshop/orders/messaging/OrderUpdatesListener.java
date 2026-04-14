package com.coffeeshop.orders.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.coffeeshop.common.domain.OrderStatus;
import com.coffeeshop.orders.domain.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;

@ApplicationScoped
public class OrderUpdatesListener {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static final class OrderUpdate {
    public Long orderId;
    public String status;
    public String from;
  }

  @Inject
  OrderRepository repo;

  @Incoming("order-updates")
  @Transactional
  public void apply(String payload) {
    try {
      if (payload == null) return;
      String json = payload.trim();

      // Unwrap if the payload is a *stringified* JSON object.
      int safety = 3;
      while (safety-- > 0
          && json.length() >= 2
          && json.charAt(0) == '"'
          && json.charAt(json.length() - 1) == '"') {
        json = MAPPER.readValue(json, String.class).trim();
      }

      OrderUpdate evt = MAPPER.readValue(json, OrderUpdate.class);
      if (evt == null || evt.status == null || evt.orderId == null) {
        System.err.println("[order-updates] ignored event: " + json);
        return;
      }

      var entity = repo.find(evt.orderId);
      if (entity == null) return;
      entity.setStatus(OrderStatus.valueOf(evt.status.toUpperCase(Locale.ROOT)));

    } catch (Exception e) {
      System.err.println("[order-updates] parse/update error: " + e + " payload=" + payload);
    }
  }
}
