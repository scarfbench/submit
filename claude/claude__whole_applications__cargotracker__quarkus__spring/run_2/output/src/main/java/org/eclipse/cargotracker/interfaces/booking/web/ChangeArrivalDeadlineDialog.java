package org.eclipse.cargotracker.interfaces.booking.web;

import java.io.Serializable;
import org.springframework.stereotype.Component;

@Component
public class ChangeArrivalDeadlineDialog implements Serializable {

  private static final long serialVersionUID = 1L;

  public void showDialog(String trackingId) {
    // Dialog functionality removed - Spring Boot does not use JSF/PrimeFaces dialogs
  }

  public void handleReturn(Object event) {
    // Dialog functionality removed
  }

  public void cancel() {
    // Dialog functionality removed
  }
}
