package Services;

import Aggregate.Order;
import Models.PaymentResult;
import Ports.PaymentGateway;
import enums.PaymentFailureReason;
import enums.PaymentStatus;

public class PaymentService implements PaymentGateway {

    @Override
    public PaymentResult process(Order order) {
        return new PaymentResult(PaymentStatus.SUCCESS, null); // happy path
    }

}
