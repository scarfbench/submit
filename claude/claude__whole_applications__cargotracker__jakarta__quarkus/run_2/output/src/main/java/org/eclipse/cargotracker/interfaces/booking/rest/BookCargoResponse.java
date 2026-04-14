package org.eclipse.cargotracker.interfaces.booking.rest;

public class BookCargoResponse {
    private String trackingId;

    public BookCargoResponse() {}
    public BookCargoResponse(String trackingId) { this.trackingId = trackingId; }
    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
}
