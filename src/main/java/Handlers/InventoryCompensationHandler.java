package Handlers;

import java.util.List;
import java.util.UUID;

import Aggregate.Order;
import Aggregate.SystemState;
import Events.Event;
import Events.InventoryReleasedEvent;
import Events.OrderCancelledEvent;
import Repositorys.OrderRepository;
import enums.InventoryState;

public class InventoryCompensationHandler implements EventHandler<OrderCancelledEvent> {
    private final OrderRepository orderRepository;

    public InventoryCompensationHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public boolean canApply(OrderCancelledEvent event, SystemState state) {
        if(state.getInventoryState() != InventoryState.RESERVED) return false;

        return true;
    }

    @Override
    public List<Event> apply(OrderCancelledEvent event, SystemState state) {
        Order order = orderRepository.findById(event.getOrderId())
            .orElseThrow();

        order.markInventoryReleased();
        orderRepository.save(order);
        return List.of(new InventoryReleasedEvent(
            UUID.randomUUID().toString(),
            event.getOrderId()
        ));
    }

}
