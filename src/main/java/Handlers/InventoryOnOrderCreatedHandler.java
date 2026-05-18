package Handlers;

import java.util.List;
import java.util.UUID;

import Aggregate.Order;
import Aggregate.SystemState;
import Events.Event;
import Events.InventoryReservationFailedEvent;
import Events.InventoryReservedEvent;
import Events.OrderCreatedEvent;
import Models.StockResult;
import Repositorys.OrderRepository;
import Services.InventoryService;
import enums.InventoryState;
import enums.OrderState;

public class InventoryOnOrderCreatedHandler implements EventHandler<OrderCreatedEvent> {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    public InventoryOnOrderCreatedHandler(OrderRepository orderRepository, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    public boolean canApply(OrderCreatedEvent event, SystemState state) {
        return state.getOrderState() != OrderState.CANCELLED
                && state.getOrderState() != OrderState.COMPLETED
                && state.getInventoryState() != InventoryState.RESERVED
                && state.getInventoryState() != InventoryState.RELEASED;
    }

    @Override
    public List<Event> apply(OrderCreatedEvent event, SystemState state) {
        return processReserveInventory(event.getOrderId(), 0);
    }

    public List<Event> processReserveInventory(String orderId, int retryCount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        StockResult result = inventoryService.checkAvailability(order.getItems());


        if (result.isAvailable()) {

            return List.of(
                    new InventoryReservedEvent(UUID.randomUUID().toString(), orderId));
        }

        return List.of(new InventoryReservationFailedEvent(
                UUID.randomUUID().toString(),
                orderId,
                0,
                result.getReason()));
    }
}