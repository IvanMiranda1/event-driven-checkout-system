package Handlers;

import java.util.List;
import java.util.UUID;

import Aggregate.Order;
import Aggregate.SystemState;
import Events.Event;
import Events.OrderCancelationRequestedEvent;
import Events.OrderCancelledEvent;
import Repositorys.OrderRepository;
import enums.OrderState;

public class OrderCancelationHandler implements EventHandler<OrderCancelationRequestedEvent> {

    private final OrderRepository orderRepository;

    // constructor
    public OrderCancelationHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public boolean canApply(OrderCancelationRequestedEvent event, SystemState state) {
        if (state.getOrderState() == OrderState.CANCELLED)
            return false;
        if (state.getOrderState() == OrderState.COMPLETED)
            return false;

        return true;
    }

    @Override
    public List<Event> apply(OrderCancelationRequestedEvent event, SystemState state) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow();

        order.cancelOrder();
        orderRepository.save(order);

        return List.of(new OrderCancelledEvent(
                UUID.randomUUID().toString(),
                event.getOrderId()));
    }
}
