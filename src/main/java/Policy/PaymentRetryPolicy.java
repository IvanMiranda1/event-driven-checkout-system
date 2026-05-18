package Policy;

import enums.PaymentFailureReason;
import enums.RetryDecision;

public class PaymentRetryPolicy {
    public static final Integer MAX_RETRIES = 3;

    public RetryDecision decide(PaymentFailureReason reason, int retryCount) {

        // 1. Casos NO retryable -> cancelar directo
        if (isNoRetryable(reason, retryCount)) {
            return RetryDecision.CANCEL;
        }

        // 2. Casos retryables
        if (isRetryable(reason)) {

            if (retryCount >= maxRetriesFor(reason)) {
                return RetryDecision.CANCEL;
            }

            return RetryDecision.RETRY;
        }

        // 3. fallback
        return RetryDecision.NOOP;

    }

    public boolean isRetryable(PaymentFailureReason reason) {
        return switch (reason) {
            case GATEWAY_TIMEOUT, NETWORK_ERROR, PROCESSOR_UNAVAILABLE, RATE_LIMITED -> true;
            default -> false;
        };
    }

    public boolean isNoRetryable(PaymentFailureReason reason, int retryCount) {
        return switch (reason) {
            case INSUFFICIENT_FUNDS, CARD_DECLINED, FRAUD_SUSPECTED, INVALID_PAYMENT_METHOD -> true;
            default -> false;
        };
    }

    private int maxRetriesFor(PaymentFailureReason reason) {
        return switch (reason) {
            case GATEWAY_TIMEOUT, NETWORK_ERROR, PROCESSOR_UNAVAILABLE -> 3;
            case RATE_LIMITED -> 3;
            case PROCESSING_ERROR -> 2;
            default -> 0;
        };
    }
}