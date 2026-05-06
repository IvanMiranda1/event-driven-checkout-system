package enums;

public enum PaymentFailureReason {
    //NO retry
    INSUFFICIENT_FUNDS,
    CARD_DECLINED,
    FRAUD_SUSPECTED,
    INVALID_PAYMENT_METHOD,
    //Fallos recuperables (retry con backoff)
    GATEWAY_TIMEOUT,
    NETWORK_ERROR,
    PROCESSOR_UNAVAILABLE,
    RATE_LIMITED,
    //Fallos ambiguos -> retry limitado y cancelar
    UNKNOWN,
    PROCESSING_ERROR
}
