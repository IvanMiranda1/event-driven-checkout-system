package Events;

import java.time.Instant;

import enums.PaymentFailureReason;


public class PaymentFailedEvent implements Event {
    String eventId;
    String orderId;
    String paymentId;
    Instant ocurredAt;
    PaymentFailureReason reason;
    Integer retryCount;

    public PaymentFailedEvent(String eventId, String orderId, PaymentFailureReason reason, Integer retryCount){
        this.eventId = eventId;
        this.orderId = orderId;
        this.ocurredAt = Instant.now();
        this.reason = reason;
        this.retryCount = retryCount;
    }


    public PaymentFailedEvent(String eventId, String orderId, PaymentFailureReason reason){
        this.eventId = eventId;
        this.orderId = orderId;
        this.ocurredAt = Instant.now();
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

    public PaymentFailureReason getReason() {
        return reason;
    }

    public Integer getRetryCount() {
        return retryCount;
    }


}

/*
timeout
network error
provider down
*/