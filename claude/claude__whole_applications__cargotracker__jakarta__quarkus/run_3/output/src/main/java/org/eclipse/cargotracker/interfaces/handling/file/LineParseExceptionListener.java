package org.eclipse.cargotracker.interfaces.handling.file;

import java.util.logging.Logger;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Batch skip read listener - converted from Jakarta Batch to stub for Quarkus.
 * Batch processing is not directly supported in Quarkus.
 * This would need to be replaced with a Quarkus-compatible approach.
 */
@Dependent
public class LineParseExceptionListener {
  @Inject private Logger logger;

  // Batch processing is not directly supported in Quarkus
  // This would need to be replaced with a Quarkus-compatible approach
}
