package Handlers;

import java.util.List;
import java.util.UUID;

import Aggregate.Order;
import Aggregate.SystemState;
import Events.Event;
import Events.OrderCompletedEvent;
import Events.PaymentSucceededEvent;
import Repositorys.OrderRepository;
import enums.OrderState;
import enums.PaymentState;

public class PaymentSucceededHandler implements EventHandler<PaymentSucceededEvent> {

    // dependencias
    private final OrderRepository orderRepository;

    // constructor
    public PaymentSucceededHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public boolean canApply(PaymentSucceededEvent event, SystemState state) {

        if (state.getOrderState() == OrderState.CANCELLED)
            return false;

        if (state.getPaymentState() == PaymentState.SUCCEEDED)
            return false;

        if (state.getPaymentState() != PaymentState.PROCESSING)
            return false;

        return true;
    }

    @Override
    public List<Event> apply(PaymentSucceededEvent event, SystemState state) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow();

        // Exactly-once semántico
        boolean wasCompletedBefore = order.getOrderState() == OrderState.COMPLETED;

        order.markPaymentSucceeded();
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
