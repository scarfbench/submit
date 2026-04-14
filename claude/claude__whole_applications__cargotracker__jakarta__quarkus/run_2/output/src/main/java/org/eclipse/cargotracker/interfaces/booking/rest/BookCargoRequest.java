package org.eclipse.cargotracker.interfaces.booking.rest;

import java.time.LocalDate;

public class BookCargoRequest {
    private String origin;
    private String destination;
    private LocalDate arrivalDeadline;

    public BookCargoRequest() {}

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDate getArrivalDeadline() { return arrivalDeadline; }
    public void setArrivalDeadline(LocalDate arrivalDeadline) { this.arrivalDeadline = arrivalDeadline; }
}
