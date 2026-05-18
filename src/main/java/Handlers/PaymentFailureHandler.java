package Handlers;

import java.util.List;
import java.util.UUID;

import Aggregate.SystemState;
import Events.Event;
import Events.OrderCancelationRequestedEvent;
import Events.PaymentFailedEvent;
import Events.PaymentRetryRequestedEvent;
import Policy.PaymentRetryPolicy;
import enums.OrderState;
import enums.PaymentFailureReason;
import enums.PaymentState;
import enums.RetryDecision;

public class PaymentFailureHandler implements EventHandler<PaymentFailedEvent> {

    private final PaymentRetryPolicy retryPolicy;

    public PaymentFailureHandler(PaymentRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    @Override
    public boolean canApply(PaymentFailedEvent event, SystemState state) {
        return state.getOrderState() != OrderState.CANCELLED
                && state.getPaymentState() == PaymentState.PROCESSING;
    }

    @Override
    public List<Event> apply(PaymentFailedEvent event, SystemState state) {

        RetryDecision decision = retryPolicy.decide(event.getReason(), event.getRetryCount());

        return switch (decision) {
            case RETRY -> List.of(retryEvent(event.getOrderId(), event.getReason(), event.getRetryCount() + 1));
            case CANCEL -> List.of(cancelEvent(event.getOrderId()));
            case NOOP -> List.of();
        };

    }

    private Event retryEvent(String orderId, PaymentFailureReason reason, int retryCount) {
        return new PaymentRetryRequestedEvent(
                UUID.randomUUID().toString(),
                orderId,
                reason,
                retryCount);
    }

    private Event cancelEvent(String orderId) {
        return new OrderCancelationRequestedEvent(
                UUID.randomUUID().toString(),
                orderId);
    }
}
