package Models;

import enums.PaymentFailureReason;
import enums.PaymentStatus;

public class PaymentResult {
    PaymentStatus status;
    PaymentFailureReason reason;

    public PaymentResult(PaymentStatus status, PaymentFailureReason reason) {
        this.status = status;
        this.reason = reason;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentFailureReason getReason() {
        return reason;
    }
    
}
