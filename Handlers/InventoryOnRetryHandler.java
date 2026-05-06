package Handlers;

import java.util.List;
import java.util.UUID;

import Aggregate.Order;
import Aggregate.SystemState;
import Events.Event;
import Events.InventoryReservationFailedEvent;
import Events.InventoryReservationRetryEvent;
import Events.InventoryReservedEvent;
import Events.OrderCompletedEvent;
import Models.StockResult;
import Repositorys.OrderRepository;
import Services.InventoryService;
import enums.InventoryState;
import enums.OrderState;

/* “La lógica se repite porque múltiples flujos convergen en la misma acción,
pero los puntos de entrada deben permanecer desacoplados” */
public class InventoryOnRetryHandler implements EventHandler<InventoryReservationRetryEvent> {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    public InventoryOnRetryHandler(OrderRepository orderRepository, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public boolean canApply(InventoryReservationRetryEvent event, SystemState state) {
        return state.getOrderState() != OrderState.CANCELLED
                && state.getOrderState() != OrderState.COMPLETED
                && state.getInventoryState() != InventoryState.RESERVED;
    }

    @Override
    public List<Event> apply(InventoryReservationRetryEvent event, SystemState state) {
        return reserveInventory(event.getOrderId(), event.getRetryCount());
    }

    public List<Event> reserveInventory(String orderId, int retryCount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        StockResult result = inventoryService.checkAvailability(order.getItems());

        if (!result.isAvailable()) {
            return List.of(new InventoryReservationFailedEvent(
                    UUID.randomUUID().toString(),
                    orderId,
                    retryCount + 1,
                    result.getReason()));
        }

        // Exactly-once semántico
        boolean wasCompletedBefore = order.getOrderState() == OrderState.COMPLETED;

        order.markInventoryReserved();
        orderRepository.save(order);

        boolean isCompletedNow = order.getOrderState() == OrderState.COMPLETED;

        if (!wasCompletedBefore && isCompletedNow) {
            return List.of(
                    new InventoryReservedEvent(UUID.randomUUID().toString(), orderId),
                    new OrderCompletedEvent(UUID.randomUUID().toString(), orderId));
        }

        return List.of(
                new InventoryReservedEvent(UUID.randomUUID().toString(), orderId));

    }

}
