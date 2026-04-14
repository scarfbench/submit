package org.eclipse.cargotracker.interfaces.booking.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Dialog handler for changing cargo arrival deadline.
 *
 * <p>Migrated from JSF to Spring Boot. PrimeFaces dialog handling has been removed.
 * This class is kept for compatibility but dialog operations should be replaced
 * with REST API calls in Spring Boot.
 */
@Component("changeArrivalDeadlineDialog")
@Scope("prototype")
public class ChangeArrivalDeadlineDialog implements Serializable {

  private static final long serialVersionUID = 1L;

  public void showDialog(String trackingId) {
    Map<String, Object> options = new HashMap<>();
    options.put("modal", true);
    options.put("draggable", true);
    options.put("resizable", false);
    options.put("contentWidth", 410);
    options.put("contentHeight", 280);

    Map<String, List<String>> params = new HashMap<>();
    List<String> values = new ArrayList<>();
    values.add(trackingId);
    params.put("trackingId", values);

    // PrimeFaces dialog opening removed - would be handled by REST API in Spring Boot
  }

  public void handleReturn(Object event) {
    // Event handling removed - would be handled by REST API in Spring Boot
  }

  public void cancel() {
    // Dialog closing removed - would be handled by REST API in Spring Boot
  }
}
