package org.eclipse.cargotracker.application.util;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ApplicationSettings implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private Long id;

  private boolean sampleLoaded = false;

  public boolean isSampleLoaded() {
    return sampleLoaded;
  }

  public void setSampleLoaded(boolean sampleLoaded) {
    this.sampleLoaded = sampleLoaded;
  }
}
