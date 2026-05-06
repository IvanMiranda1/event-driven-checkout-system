package Handlers;

import java.util.List;
import java.util.UUID;

import Aggregate.Order;
import Aggregate.SystemState;
import Events.Event;
import Events.InventoryReservedEvent;
import Events.OrderCompletedEvent;
import Repositorys.OrderRepository;
import enums.InventoryState;
import enums.OrderState;

public class InventoryReservedHandler implements EventHandler<InventoryReservedEvent> {

    private final OrderRepository orderRepository;

    // constructor
    public InventoryReservedHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public boolean canApply(InventoryReservedEvent event, SystemState state) {
        if (state.getOrderState() == OrderState.CANCELLED)
            return false;

        if (state.getInventoryState() == InventoryState.RESERVED)
            return false;

        if (state.getInventoryState() != InventoryState.NOT_RESERVED)
            return false;

        return true;
    }

    @Override
    public List<Event> apply(InventoryReservedEvent event, SystemState steate) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow();

        // Exactly-once semántico
        boolean wasCompletedBefore = order.getOrderState() == OrderState.COMPLETED;

        order.markInventoryReserved();
        orderRepository.save(order);

        boolean isCompletedNow = order.getOrderState() == OrderState.COMPLETED;


        if (!wasCompletedBefore && isCompletedNow) {
            return List.of(
                    new OrderCompletedEvent(UUID.randomUUID().toString(), event.getOrderId()));
        }

        // No hay mas nada que emitir
        return List.of();

    }

}
