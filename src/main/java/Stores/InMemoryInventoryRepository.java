package Stores;

import java.util.HashMap;
import java.util.Map;

import Repositorys.InventoryRepository;

public class InMemoryInventoryRepository implements InventoryRepository {

    private final Map<String, Integer> stock = new HashMap<>();

    // para setup (test / main)
    public void setStock(String itemId, int quantity) {
        stock.put(itemId, quantity);
    }

    // consulta simple
    public Integer getStock(String itemId) {
        return stock.getOrDefault(itemId, 0);
    }

    // opcional: para debug
    public void printStock() {
        System.out.println(stock);
    }

    public Integer checkAvailableStock() {
        return 0;
    }
}