package Events;
// Event.java

import java.time.Instant;

public interface Event {
    String getEventId();
    String getOrderId();
    Instant getOcurredAt();


}