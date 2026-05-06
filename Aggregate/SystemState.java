package Aggregate;
import enums.InventoryState;
import enums.OrderState;
import enums.PaymentState;

public class SystemState {
    
    private final String orderId;
    private final OrderState orderState;
    private final PaymentState paymentState;
    private final InventoryState inventoryState;

    public SystemState(String orderId, OrderState orderState, PaymentState paymentState, InventoryState inventoryState) {
        this.orderId = orderId;
        this.orderState = orderState;
        this.paymentState = paymentState;
        this.inventoryState = inventoryState;
    }

     public String getOrderId() {
        return orderId;
    }
    
    public OrderState getOrderState() {
        return orderState;
    }

    public PaymentState getPaymentState() {
        return paymentState;
    }

    public InventoryState getInventoryState() {
        return inventoryState;
    }
}
/* 
importante
> SystemState es un DTO de lectura

+ no tiene lógica
+ no modifica nada
+ solo transporta datos
*/ 