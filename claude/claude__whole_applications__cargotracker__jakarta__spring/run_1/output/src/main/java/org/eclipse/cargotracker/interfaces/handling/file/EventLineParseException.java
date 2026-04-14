package org.eclipse.cargotracker.interfaces.handling.file;

/**
 * Exception for parsing event file lines.
 * No changes needed - plain exception class.
 */
public class EventLineParseException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String line;

  public EventLineParseException(String message, Throwable cause, String line) {
    super(message, cause);
    this.line = line;
  }

  public EventLineParseException(String message, String line) {
    super(message);
    this.line = line;
  }

  public String getLine() {
    return line;
  }
}
