package org.eclipse.cargotracker.interfaces.booking.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// import jakarta.enterprise.context.SessionScoped;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
// import org.primefaces.PrimeFaces;
// import org.primefaces.event.SelectEvent;

/**
 * NOTE: JSF/PrimeFaces-specific functionality has been commented out for Spring Boot migration.
 * This class structure is preserved but dialog operations are disabled.
 */
@Component("changeArrivalDeadlineDialog")
@SessionScope
public class ChangeArrivalDeadlineDialog implements Serializable {

  private static final long serialVersionUID = 1L;

  public void showDialog(String trackingId) {
    // PrimeFaces dialog functionality commented out (JSF-specific)
    // Map<String, Object> options = new HashMap<>();
    // options.put("modal", true);
    // options.put("draggable", true);
    // options.put("resizable", false);
    // options.put("contentWidth", 410);
    // options.put("contentHeight", 280);
    //
    // Map<String, List<String>> params = new HashMap<>();
    // List<String> values = new ArrayList<>();
    // values.add(trackingId);
    // params.put("trackingId", values);
    //
    // PrimeFaces.current()
    //     .dialog()
    //     .openDynamic("/admin/dialogs/change_arrival_deadline.xhtml", options, params);
  }

  public void handleReturn(Object event) {
    // @SuppressWarnings("rawtypes") SelectEvent event - JSF-specific
  }

  public void cancel() {
    // just kill the dialog
    // PrimeFaces.current().dialog().closeDynamic(""); // JSF-specific
  }
}
