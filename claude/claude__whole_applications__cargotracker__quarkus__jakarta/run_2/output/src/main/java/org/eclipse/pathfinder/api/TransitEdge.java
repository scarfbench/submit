package org.eclipse.pathfinder.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import jakarta.json.bind.annotation.JsonbTransient;

/** Represents an edge in a path through a graph, describing the route of a cargo. */
public class TransitEdge implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonbTransient
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private String voyageNumber;
  private String fromUnLocode;
  private String toUnLocode;
  private String fromDate;
  private String toDate;

  public TransitEdge() {
    // Nothing to do.
  }

  public TransitEdge(
      String voyageNumber,
      String fromUnLocode,
      String toUnLocode,
      LocalDateTime fromDate,
      LocalDateTime toDate) {
    this.voyageNumber = voyageNumber;
    this.fromUnLocode = fromUnLocode;
    this.toUnLocode = toUnLocode;
    this.fromDate = fromDate != null ? fromDate.format(FORMATTER) : null;
    this.toDate = toDate != null ? toDate.format(FORMATTER) : null;
  }

  public String getVoyageNumber() {
    return voyageNumber;
  }

  public void setVoyageNumber(String voyageNumber) {
    this.voyageNumber = voyageNumber;
  }

  public String getFromUnLocode() {
    return fromUnLocode;
  }

  public void setFromUnLocode(String fromUnLocode) {
    this.fromUnLocode = fromUnLocode;
  }

  public String getToUnLocode() {
    return toUnLocode;
  }

  public void setToUnLocode(String toUnLocode) {
    this.toUnLocode = toUnLocode;
  }

  /** Returns fromDate as String for JSON-B serialization. */
  public String getFromDate() {
    return fromDate;
  }

  public void setFromDate(String fromDate) {
    this.fromDate = fromDate;
  }

  /** Returns toDate as String for JSON-B serialization. */
  public String getToDate() {
    return toDate;
  }

  public void setToDate(String toDate) {
    this.toDate = toDate;
  }

  /** Returns fromDate as LocalDateTime for domain code use. */
  @JsonbTransient
  public LocalDateTime getFromDateTime() {
    return fromDate != null ? LocalDateTime.parse(fromDate, FORMATTER) : null;
  }

  /** Returns toDate as LocalDateTime for domain code use. */
  @JsonbTransient
  public LocalDateTime getToDateTime() {
    return toDate != null ? LocalDateTime.parse(toDate, FORMATTER) : null;
  }

  @Override
  public String toString() {
    return "TransitEdge{"
        + "voyageNumber="
        + voyageNumber
        + ", fromUnLocode="
        + fromUnLocode
        + ", toUnLocode="
        + toUnLocode
        + ", fromDate="
        + fromDate
        + ", toDate="
        + toDate
        + '}';
  }
}
