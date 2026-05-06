package stores;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import Aggregate.Order;
import Repositorys.OrderRepository;

public class InMemoryOrderRepository implements OrderRepository {
    
    private final Map<String, Order> store = new HashMap<>();

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Order order) {
        store.put(order.getId(), order);
    }
    
}
