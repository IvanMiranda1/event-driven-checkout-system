package Handlers;

import java.util.List;
import java.util.UUID;

import Aggregate.SystemState;
import Events.Event;
import Events.InventoryReservationFailedEvent;
import Events.InventoryReservationRetryEvent;
import Events.OrderCancelationRequestedEvent;
import enums.InventoryFailureReason;
import enums.InventoryState;
import enums.OrderState;

//es quién decide que hacer con el fallo
public class InventoryFailureHandler implements EventHandler<InventoryReservationFailedEvent> {
    
    public static final int MAX_RETRIES = 3;

    @Override
    public boolean canApply(InventoryReservationFailedEvent event, SystemState state) {
        return state.getOrderState() != OrderState.CANCELLED
                && state.getOrderState() != OrderState.COMPLETED
                && state.getInventoryState() != InventoryState.RESERVED;
    }
    
    @Override
    public List<Event> apply(InventoryReservationFailedEvent event, SystemState state) {
        // no usamos order en handlers de decision como este...

        if (event.getReason() == InventoryFailureReason.TEMPORARY_ERROR) {

            if (event.getRetryCount() >= MAX_RETRIES) {
                return List.of(cancelEvent(event));
            }
            return List.of(retryEvent(event));
        }

        if (event.getReason() == InventoryFailureReason.OUT_OF_STOCK) {
            // event.retryCount();

            if (event.getRetryCount() >= MAX_RETRIES) {
                return List.of(cancelEvent(event));
            }

            return List.of(retryEvent(event));

            // reintentar algunas veces y luego cancelar pedido / retry backoof / noop /
            // cancelar pedido

        }
        return List.of();

    }

    private Event cancelEvent(InventoryReservationFailedEvent event) {
        return new OrderCancelationRequestedEvent(
                UUID.randomUUID().toString(),
                event.getOrderId());
    }

    private Event retryEvent(InventoryReservationFailedEvent event) {
        return new InventoryReservationRetryEvent(
                UUID.randomUUID().toString(),
                event.getOrderId(),
                event.getRetryCount() + 1,
                event.getReason()
         );
    }

}
