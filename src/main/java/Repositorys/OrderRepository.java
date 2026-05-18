package Repositorys;
import java.util.Optional;

import Aggregate.Order;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(String orderId);

}