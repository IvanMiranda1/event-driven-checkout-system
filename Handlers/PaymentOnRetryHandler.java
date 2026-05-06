package Handlers;

import java.util.List;
import java.util.UUID;

import Aggregate.Order;
import Aggregate.SystemState;
import Events.Event;
import Events.PaymentFailedEvent;
import Events.PaymentRetryRequestedEvent;
import Events.PaymentSucceededEvent;
import Models.PaymentResult;
import Repositorys.OrderRepository;
import Services.PaymentService;
import enums.OrderState;
import enums.PaymentFailureReason;
import enums.PaymentState;
import enums.PaymentStatus;

public class PaymentOnRetryHandler implements EventHandler<PaymentRetryRequestedEvent> {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    public PaymentOnRetryHandler(OrderRepository orderRepository, PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }

    @Override
    public boolean canApply(PaymentRetryRequestedEvent event, SystemState state) {
        return state.getOrderState() != OrderState.CANCELLED
                && state.getPaymentState() != PaymentState.SUCCEEDED;
    }

    @Override
    public List<Event> apply(PaymentRetryRequestedEvent event, SystemState state) {
        return processPayment(event.getOrderId(), event.getRetryCount());
    }

    public List<Event> processPayment(String orderId, int retryCount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow();

        // necesito obtener el reason
        PaymentResult result = paymentService.process(order);

        if (result.getStatus() == PaymentStatus.SUCCESS) {
            return List.of(successEvent(orderId));
        }

        // NOOP
        return List
                .of(new PaymentFailedEvent(UUID.randomUUID().toString(), orderId, result.getReason(), retryCount));
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

}
