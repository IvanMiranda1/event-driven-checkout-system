package Services;

import Aggregate.Order;
import Models.PaymentResult;
import enums.PaymentFailureReason;
import enums.PaymentStatus;

public class PaymentService {


    public PaymentResult process(Order order) {
        return new PaymentResult(PaymentStatus.FAILURE, PaymentFailureReason.GATEWAY_TIMEOUT); //reintenta 5 veces
        

        //return new PaymentResult(PaymentStatus.SUCCESS, null); // happy path
        

    }

}
