package Events;

import java.time.Instant;

public class InventoryReleasedEvent implements Event {
    String eventId;
    String orderId;
    Instant ocurredAt;

    public InventoryReleasedEvent(String eventId, String orderId){
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
