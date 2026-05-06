package Events;

import java.time.Instant;

import enums.PaymentFailureReason;

public class PaymentRetryRequestedEvent implements Event {
    String eventId;
    String orderId;
    Integer retryCount;
    Instant ocurredAt;
    PaymentFailureReason reason;

    public PaymentRetryRequestedEvent(String eventId, String orderId, Integer retryCount){
        this.eventId = eventId;
        this.orderId = orderId;
        this.ocurredAt = Instant.now();
        this.retryCount = retryCount;
    }
    
    public PaymentRetryRequestedEvent(String eventId, String orderId, PaymentFailureReason reason, Integer retryCount){
        this.eventId = eventId;
        this.orderId = orderId;
        this.ocurredAt = Instant.now();
        this.retryCount = retryCount;
        this.reason = reason;
    }

    @Override
    public Instant getOcurredAt() {
        return ocurredAt;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

    public Integer getRetryCount(){
        return retryCount;
    }

    
}