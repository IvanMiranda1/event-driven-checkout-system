package Events;

import java.time.Instant;

import enums.InventoryFailureReason;

public class InventoryReservationFailedEvent implements Event {
    String eventId;
    String orderId;
    Instant ocurredAt;
    // Observabilidad -> debug / logs / metricas -> desaparecen en el --
    // InventoryReservationRetryEvent porque aun no aplicare eso
    Integer requestedQuantity;
    Integer availableStock;
    InventoryFailureReason reason; // es lo que necesita el sistema para decidir - retry / cancel / NOOP -> alcanza
                                   // con reason y retryCount
    Integer retryCount;

    public InventoryReservationFailedEvent(String eventId, String orderId, Integer requestedQuantity,
            Integer availableStock, InventoryFailureReason reason, Integer retryCount) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
        this.reason = reason;
        this.ocurredAt = Instant.now();
        this.retryCount = retryCount;
    }

    //se usa en InventoryOnOrderCreatedHandler
    public InventoryReservationFailedEvent(String eventId, String orderId) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.ocurredAt = Instant.now();
    }

    public InventoryReservationFailedEvent(String eventId, String orderId, Integer retryCount, InventoryFailureReason reason) {
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

    public InventoryFailureReason getReason() {
        return reason;
    }

    public void retryCount() {
        retryCount++;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    // Diferencia entre Requested and available
    public Integer unfulfilledAmount() {
        return availableStock - requestedQuantity;
    }

}
