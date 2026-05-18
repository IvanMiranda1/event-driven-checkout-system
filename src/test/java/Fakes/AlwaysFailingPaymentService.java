package Fakes;

import Aggregate.Order;
import Models.PaymentResult;
import Ports.PaymentGateway;
import enums.PaymentFailureReason;
import enums.PaymentStatus;

public class AlwaysFailingPaymentService implements PaymentGateway {

    @Override
    public PaymentResult process(Order order) {
        return new PaymentResult(PaymentStatus.FAILURE, PaymentFailureReason.GATEWAY_TIMEOUT); // reintenta 5 veces

    }

}
