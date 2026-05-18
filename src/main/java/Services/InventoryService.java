package Services;


import java.util.List;

import Models.Item;
import Models.StockResult;
import Repositorys.InventoryRepository;

public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryService(){
        this(null);
    }

    public StockResult checkAvailability(List<Item> items){

        int requested = items.size();

        int available = 0;

        for(Item item : items){
            available += inventoryRepository.getStock(item.getItemId());
        }

        boolean isAvailable = available >= requested;
        
        return new StockResult(isAvailable, available, requested);

    }
    
}
