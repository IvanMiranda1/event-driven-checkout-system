package Aggregate;

import java.util.List;

import Models.Item;
import enums.InventoryState;
import enums.OrderState;
import enums.PaymentState;

public class Order {

    private String id;
    private OrderState orderState;
    private PaymentState paymentState;
    private InventoryState inventoryState;
    private List<Item> items;

    public Order(String id, OrderState orderState, PaymentState paymentState, InventoryState inventoryState,
            List<Item> items) {
        this.id = id;
        this.orderState = orderState;
        this.paymentState = paymentState;
        this.inventoryState = inventoryState;
        this.items = items;
    }

    // markX():
    // -> cambia estado
    // -> agrega eventos si corresponde

    // Payment
    public void markPaymentSucceeded() {
        // invariantes
        if (orderState == OrderState.CANCELLED)
            throw new IllegalStateException("No se puede procesar un pedido que ya está CANCELADO");
        if (orderState == OrderState.COMPLETED)
            return;

        if (paymentState == PaymentState.FAILED) {
            throw new IllegalStateException("No se puede procesar un pedido con pago FALLIDO");
        }
        if (paymentState == PaymentState.SUCCEEDED)
            return;// idempotencia

        // cambia estado
        // solo permito processing → succeeded
        // todo lo demás → error
        if (paymentState == PaymentState.PROCESSING) {
            this.paymentState = PaymentState.SUCCEEDED;
        } else {
            throw new IllegalStateException("No se puede marcar el pago como SUCCEEDED si no está en PROCESSING");
        }

        checkCompletion();
    }

    public void markPaymentFailed() {
        if (orderState == OrderState.CANCELLED)
            throw new IllegalStateException();
        if (orderState == OrderState.COMPLETED)
            return;

        if (paymentState == PaymentState.SUCCEEDED)
            throw new IllegalStateException();
        if (paymentState == PaymentState.FAILED)
            return;

        if (paymentState != PaymentState.PROCESSING) {
            throw new IllegalStateException();
        }

        this.paymentState = PaymentState.FAILED;
    }

    // Inventory
    public void markInventoryReserved() {
        if (orderState == OrderState.CANCELLED)
            throw new IllegalStateException("no se puede procesar pedido que ya esta CANCELADO");
        if (orderState == OrderState.COMPLETED)
            return;

        if (inventoryState == InventoryState.RELEASED)
            return;
        if (inventoryState == InventoryState.RESERVED)
            return;

        if (inventoryState == InventoryState.NOT_RESERVED) {
            this.inventoryState = InventoryState.RESERVED;
        } else {
            throw new IllegalStateException(
                    "No se puede marcar el inventario como RESERVED si no está en NOT_RESERVED");
        }
        checkCompletion();
    }

    public void markInventoryReleased() {
        if (orderState == OrderState.CANCELLED)
            throw new IllegalStateException();

        if (inventoryState == InventoryState.NOT_RESERVED)
            return;

        if (orderState == OrderState.COMPLETED)
            return;

        if (inventoryState == InventoryState.RESERVED) {
            this.inventoryState = InventoryState.RELEASED;
        }
    }

    // Order
    private void checkCompletion() {

        if (orderState == OrderState.CANCELLED)
            return;

        if (orderState == OrderState.COMPLETED)
            return;

        if (paymentState == PaymentState.SUCCEEDED &&
                inventoryState == InventoryState.RESERVED) {
            orderState = OrderState.COMPLETED;
        }

    }

    public void cancelOrder() {
        if (orderState == OrderState.COMPLETED)
            throw new IllegalStateException();

        if (orderState == OrderState.CANCELLED)
            return;

        this.orderState = OrderState.CANCELLED;
    }

    // GETTERS
    public String getId() {
        return id;
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

    public List<Item> getItems() {
        return items;
    }
}
