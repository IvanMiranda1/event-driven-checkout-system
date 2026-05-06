package Repositorys;



public interface InventoryRepository {
    Integer checkAvailableStock();
    Integer getStock(String itemId);
    void setStock(String itemId, int quantity);
    void printStock();


}
