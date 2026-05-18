package Handlers;

import java.util.List;
import java.util.UUID;

import Aggregate.Order;
import Aggregate.SystemState;
import Events.Event;
import Events.OrderCreatedEvent;
import Events.PaymentFailedEvent;
import Events.PaymentRetryRequestedEvent;
import Events.PaymentSucceededEvent;
import Models.PaymentResult;
import Ports.PaymentGateway;
import Repositorys.OrderRepository;
import enums.OrderState;
import enums.PaymentFailureReason;
import enums.PaymentState;
import enums.PaymentStatus;

public class PaymentOnOrderCreatedHandler implements EventHandler<OrderCreatedEvent> {

    private final OrderRepository orderRepository;
    private final PaymentGateway paymentService;

    public PaymentOnOrderCreatedHandler(OrderRepository orderRepository, PaymentGateway paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }

    @Override
    public boolean canApply(OrderCreatedEvent event, SystemState state) {
        return state.getOrderState() != OrderState.CANCELLED
                && state.getPaymentState() != PaymentState.SUCCEEDED;
    }
     
    @Override
    public List<Event> apply(OrderCreatedEvent event, SystemState state) {
        return processPayment(event.getOrderId(), 0);
    }

    public List<Event> processPayment(String orderId, int retryCount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        // necesito obtener el reason
        PaymentResult result = paymentService.process(order);

        if (result.getStatus() == PaymentStatus.SUCCESS) {
            return List.of(successEvent(orderId));
        }

        // Failure
        return List.of(failedEvent(orderId, result.getReason()));
    }

    public Event retryEvent(String orderId, PaymentFailureReason reason, int retry) {
        return new PaymentRetryRequestedEvent(
                UUID.randomUUID().toString(),
                orderId,
                reason,
                retry);
    }

    public Event cancelEvent(String orderId, PaymentFailureReason reason) {
        return new PaymentFailedEvent(
                UUID.randomUUID().toString(),
                orderId,
                reason);

    }

    public Event successEvent(String orderId) {
        return new PaymentSucceededEvent(
                UUID.randomUUID().toString(),
                orderId);
    }

    public Event failedEvent(String orderId, PaymentFailureReason reason) {
        return new PaymentFailedEvent(
                UUID.randomUUID().toString(),
                orderId,
                reason,
                0);
    }

}
/*
 * ✔️ Ideal (más limpio)
 * 
 * Separar:
 * 
 * PaymentFailedEvent → fallo técnico
 * OrderCancelationRequested → decisión de negocio
 */