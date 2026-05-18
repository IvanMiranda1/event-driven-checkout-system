package Models;

import enums.InventoryFailureReason;

public class StockResult {
    boolean available;
    Integer requestedQuantity;
    Integer availableStock;
    InventoryFailureReason reason;


    public StockResult(boolean available, Integer availableStock) {
        this.available = available;
        this.availableStock = availableStock;
    }

    public StockResult(boolean available, Integer availableStock, Integer requestedQuantity) {
        this.available = available;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }
    

    public boolean isAvailable() {
        return available;
    }

    public Integer getAvailableStock(){
        return availableStock;
    }

    public InventoryFailureReason getReason() {
        return reason;
    }


}