package Events;

import java.time.Instant;


public class PaymentSucceededEvent implements Event {

    String eventId;
    String orderId;
    String paymentId;
    String amount;
    Instant ocurredAt;

    public PaymentSucceededEvent(String eventId, String orderId){
        this.eventId = eventId;
        this.orderId = orderId;
        this.ocurredAt = Instant.now();
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

}
