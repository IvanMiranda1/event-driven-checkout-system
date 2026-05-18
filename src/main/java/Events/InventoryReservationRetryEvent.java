package Events;

import java.time.Instant;

import enums.InventoryFailureReason;

public class InventoryReservationRetryEvent implements Event {
    String eventId;
    String orderId;
    Instant ocurredAt;
    InventoryFailureReason reason;
    Integer retryCount;

    public InventoryReservationRetryEvent(String eventId, String orderId, InventoryFailureReason reason, Integer retryCount){
        this.eventId = eventId;
        this.orderId = orderId;
        this.ocurredAt = Instant.now();
        this.reason = reason;
        this.retryCount = retryCount;
    }
    public InventoryReservationRetryEvent(String eventId, String orderId, Integer retryCount, InventoryFailureReason reason){
        this.eventId = eventId;
        this.orderId = orderId;
        this.ocurredAt = Instant.now();
        this.retryCount = retryCount;
        this.reason = reason;
    }

    public InventoryReservationRetryEvent(String eventId, String orderId){
        this.eventId = eventId;
        this.orderId = orderId;
        this.ocurredAt = Instant.now();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

    public InventoryFailureReason reason() {
        return reason;
    }

    public void retryCount() {
        retryCount++;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    // Diferencia entre Requested and available
    public Instant getOcurredAt(){
        return ocurredAt;
    }

}
