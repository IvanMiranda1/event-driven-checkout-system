
import org.junit.jupiter.api.Test;

import Aggregate.Order;
import Events.Event;
import Events.InventoryReservedEvent;
import Events.OrderCancelationRequestedEvent;
import Events.OrderCancelledEvent;
import Events.OrderCreatedEvent;
import Events.PaymentFailedEvent;
import Events.PaymentRetryRequestedEvent;
import Events.PaymentSucceededEvent;
import Fakes.AlwaysFailingPaymentService;
import Handlers.InventoryCompensationHandler;
import Handlers.InventoryOnOrderCreatedHandler;
import Handlers.InventoryReservedHandler;
import Handlers.OrderCancelationHandler;
import Handlers.PaymentFailureHandler;
import Handlers.PaymentOnOrderCreatedHandler;
import Handlers.PaymentOnRetryHandler;
import Handlers.PaymentSucceededHandler;
import Models.Item;
import Policy.PaymentRetryPolicy;
import Repositorys.InventoryRepository;
import Repositorys.OrderRepository;
import Services.EventProcessor;
import Services.InventoryService;
import Services.PaymentService;
import Stores.EventRegistry;
import Stores.InMemoryInventoryRepository;
import Stores.InMemoryOrderRepository;
import Stores.InMemoryOutbox;
import Stores.InMemoryProcessedEventStore;
import Stores.Outbox;
import Stores.ProcessedEventStore;
import enums.InventoryState;
import enums.OrderState;
import enums.PaymentState;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

public class CheckoutFlowTest {

    @Test
    void happyPath_orderShouldBeCompleted() {
        // Arrange
        // -> infra
        OrderRepository orderRepository = new InMemoryOrderRepository();
        InventoryRepository inventoryRepository = new InMemoryInventoryRepository();
        ProcessedEventStore processedStore = new InMemoryProcessedEventStore();
        Outbox outbox = new InMemoryOutbox();

        // -> services
        PaymentService paymentService = new PaymentService();
        InventoryService inventoryService = new InventoryService(inventoryRepository);
        PaymentRetryPolicy retryPolicy = new PaymentRetryPolicy();

        // -> handlers
        PaymentOnOrderCreatedHandler paymentHandler = new PaymentOnOrderCreatedHandler(orderRepository, paymentService);
        InventoryOnOrderCreatedHandler inventoryHandler = new InventoryOnOrderCreatedHandler(orderRepository,
                inventoryService);

        PaymentSucceededHandler paymentSucceededHandler = new PaymentSucceededHandler(orderRepository);
        InventoryReservedHandler inventoryReservedHandler = new InventoryReservedHandler(orderRepository);

        PaymentFailureHandler paymentFailureHandler = new PaymentFailureHandler(retryPolicy);

        PaymentOnRetryHandler paymentRetryHandler = new PaymentOnRetryHandler(orderRepository, paymentService);

        OrderCancelationHandler orderCancelationHandler = new OrderCancelationHandler(orderRepository);

        InventoryCompensationHandler inventoryCompensationHandler = new InventoryCompensationHandler(orderRepository);

        // -> registry
        EventRegistry registry = new EventRegistry();
        registry.subscribe(OrderCreatedEvent.class, paymentHandler);
        registry.subscribe(OrderCreatedEvent.class, inventoryHandler);

        registry.subscribe(PaymentSucceededEvent.class, paymentSucceededHandler);
        registry.subscribe(InventoryReservedEvent.class, inventoryReservedHandler);

        registry.subscribe(PaymentFailedEvent.class, paymentFailureHandler);
        registry.subscribe(PaymentRetryRequestedEvent.class, paymentRetryHandler);

        registry.subscribe(OrderCancelationRequestedEvent.class, orderCancelationHandler);
        registry.subscribe(OrderCancelledEvent.class, inventoryCompensationHandler);

        EventProcessor processor = new EventProcessor(orderRepository, processedStore, outbox, registry);

        inventoryRepository.setStock("item-1", 10);
        inventoryRepository.setStock("item-2", 5);

        List<Item> items = List.of(
                new Item("item-1"),
                new Item("item-1"),
                new Item("item-2"));

        Order order = new Order(
                "order-happy-path",
                OrderState.PENDING,
                PaymentState.PROCESSING,
                InventoryState.NOT_RESERVED,
                items);

        orderRepository.save(order);

        // Act
        Event event = new OrderCreatedEvent(UUID.randomUUID().toString(), order.getId());
        processor.process(event);

        // Assert
        Order result = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderState.COMPLETED, result.getOrderState());
    }

    @Test
    void retryExhaustion_orderShouldBeCancelled() {
        //Arrange
        OrderRepository orderRepository = new InMemoryOrderRepository();
        InventoryRepository inventoryRepository = new InMemoryInventoryRepository();
        ProcessedEventStore processedStore = new InMemoryProcessedEventStore();
        Outbox outbox = new InMemoryOutbox();

        // -> services
        AlwaysFailingPaymentService paymentService = new AlwaysFailingPaymentService();
        InventoryService inventoryService = new InventoryService(inventoryRepository);
        PaymentRetryPolicy retryPolicy = new PaymentRetryPolicy();

        // -> handlers
        PaymentOnOrderCreatedHandler paymentHandler = new PaymentOnOrderCreatedHandler(orderRepository, paymentService);
        InventoryOnOrderCreatedHandler inventoryHandler = new InventoryOnOrderCreatedHandler(orderRepository,
                inventoryService);

        PaymentSucceededHandler paymentSucceededHandler = new PaymentSucceededHandler(orderRepository);
        InventoryReservedHandler inventoryReservedHandler = new InventoryReservedHandler(orderRepository);

        PaymentFailureHandler paymentFailureHandler = new PaymentFailureHandler(retryPolicy);

        PaymentOnRetryHandler paymentRetryHandler = new PaymentOnRetryHandler(orderRepository, paymentService);

        OrderCancelationHandler orderCancelationHandler = new OrderCancelationHandler(orderRepository);

        InventoryCompensationHandler inventoryCompensationHandler = new InventoryCompensationHandler(orderRepository);

        // -> registry
        EventRegistry registry = new EventRegistry();
        registry.subscribe(OrderCreatedEvent.class, paymentHandler);
        registry.subscribe(OrderCreatedEvent.class, inventoryHandler);

        registry.subscribe(PaymentSucceededEvent.class, paymentSucceededHandler);
        registry.subscribe(InventoryReservedEvent.class, inventoryReservedHandler);

        registry.subscribe(PaymentFailedEvent.class, paymentFailureHandler);
        registry.subscribe(PaymentRetryRequestedEvent.class, paymentRetryHandler);

        registry.subscribe(OrderCancelationRequestedEvent.class, orderCancelationHandler);
        registry.subscribe(OrderCancelledEvent.class, inventoryCompensationHandler);

        EventProcessor processor = new EventProcessor(orderRepository, processedStore, outbox, registry);

        inventoryRepository.setStock("item-1", 10);
        inventoryRepository.setStock("item-2", 5);

        List<Item> items = List.of(
                new Item("item-1"),
                new Item("item-1"),
                new Item("item-2"));

        Order order = new Order(
                "order-happy-path",
                OrderState.PENDING,
                PaymentState.PROCESSING,
                InventoryState.NOT_RESERVED,
                items);

        orderRepository.save(order);
        //Act
        Event event = new OrderCreatedEvent(UUID.randomUUID().toString(), order.getId());
        processor.process(event);

        //Assert
        Order result = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderState.CANCELLED, result.getOrderState());
    }

    @Test
    void duplicateEvent_orderShouldNotChangeState() {
        //Arrange
        // -> infra
        OrderRepository orderRepository = new InMemoryOrderRepository();
        InventoryRepository inventoryRepository = new InMemoryInventoryRepository();
        ProcessedEventStore processedStore = new InMemoryProcessedEventStore();
        Outbox outbox = new InMemoryOutbox();

        // -> services
        PaymentService paymentService = new PaymentService();
        InventoryService inventoryService = new InventoryService(inventoryRepository);
        PaymentRetryPolicy retryPolicy = new PaymentRetryPolicy();

        // -> handlers
        PaymentOnOrderCreatedHandler paymentHandler = new PaymentOnOrderCreatedHandler(orderRepository, paymentService);
        InventoryOnOrderCreatedHandler inventoryHandler = new InventoryOnOrderCreatedHandler(orderRepository,
                inventoryService);

        PaymentSucceededHandler paymentSucceededHandler = new PaymentSucceededHandler(orderRepository);
        InventoryReservedHandler inventoryReservedHandler = new InventoryReservedHandler(orderRepository);

        PaymentFailureHandler paymentFailureHandler = new PaymentFailureHandler(retryPolicy);

        PaymentOnRetryHandler paymentRetryHandler = new PaymentOnRetryHandler(orderRepository, paymentService);

        OrderCancelationHandler orderCancelationHandler = new OrderCancelationHandler(orderRepository);

        InventoryCompensationHandler inventoryCompensationHandler = new InventoryCompensationHandler(orderRepository);

        // -> registry
        EventRegistry registry = new EventRegistry();
        registry.subscribe(OrderCreatedEvent.class, paymentHandler);
        registry.subscribe(OrderCreatedEvent.class, inventoryHandler);

        registry.subscribe(PaymentSucceededEvent.class, paymentSucceededHandler);
        registry.subscribe(InventoryReservedEvent.class, inventoryReservedHandler);

        registry.subscribe(PaymentFailedEvent.class, paymentFailureHandler);
        registry.subscribe(PaymentRetryRequestedEvent.class, paymentRetryHandler);

        registry.subscribe(OrderCancelationRequestedEvent.class, orderCancelationHandler);
        registry.subscribe(OrderCancelledEvent.class, inventoryCompensationHandler);

        EventProcessor processor = new EventProcessor(orderRepository, processedStore, outbox, registry);

        inventoryRepository.setStock("item-1", 10);
        inventoryRepository.setStock("item-2", 5);

        List<Item> items = List.of(
                new Item("item-1"),
                new Item("item-1"),
                new Item("item-2"));

        Order order = new Order(
                "order-happy-path",
                OrderState.PENDING,
                PaymentState.PROCESSING,
                InventoryState.NOT_RESERVED,
                items);

        orderRepository.save(order);

        //Act
        Event event = new OrderCreatedEvent(UUID.randomUUID().toString(), order.getId());
        processor.process(event);
        processor.process(event);

        //Assert
        Order result = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderState.COMPLETED, result.getOrderState());
    }

    @Test
    void compensationAfterPartialFailure_orderShouldBeCancelledAndInventoryReleased() {
//Arrange
        OrderRepository orderRepository = new InMemoryOrderRepository();
        InventoryRepository inventoryRepository = new InMemoryInventoryRepository();
        ProcessedEventStore processedStore = new InMemoryProcessedEventStore();
        Outbox outbox = new InMemoryOutbox();

        // -> services
        AlwaysFailingPaymentService paymentService = new AlwaysFailingPaymentService();
        InventoryService inventoryService = new InventoryService(inventoryRepository);
        PaymentRetryPolicy retryPolicy = new PaymentRetryPolicy();

        // -> handlers
        PaymentOnOrderCreatedHandler paymentHandler = new PaymentOnOrderCreatedHandler(orderRepository, paymentService);
        InventoryOnOrderCreatedHandler inventoryHandler = new InventoryOnOrderCreatedHandler(orderRepository,
                inventoryService);

        PaymentSucceededHandler paymentSucceededHandler = new PaymentSucceededHandler(orderRepository);
        InventoryReservedHandler inventoryReservedHandler = new InventoryReservedHandler(orderRepository);

        PaymentFailureHandler paymentFailureHandler = new PaymentFailureHandler(retryPolicy);

        PaymentOnRetryHandler paymentRetryHandler = new PaymentOnRetryHandler(orderRepository, paymentService);

        OrderCancelationHandler orderCancelationHandler = new OrderCancelationHandler(orderRepository);

        InventoryCompensationHandler inventoryCompensationHandler = new InventoryCompensationHandler(orderRepository);

        // -> registry
        EventRegistry registry = new EventRegistry();
        registry.subscribe(OrderCreatedEvent.class, paymentHandler);
        registry.subscribe(OrderCreatedEvent.class, inventoryHandler);

        registry.subscribe(PaymentSucceededEvent.class, paymentSucceededHandler);
        registry.subscribe(InventoryReservedEvent.class, inventoryReservedHandler);

        registry.subscribe(PaymentFailedEvent.class, paymentFailureHandler);
        registry.subscribe(PaymentRetryRequestedEvent.class, paymentRetryHandler);

        registry.subscribe(OrderCancelationRequestedEvent.class, orderCancelationHandler);
        registry.subscribe(OrderCancelledEvent.class, inventoryCompensationHandler);

        EventProcessor processor = new EventProcessor(orderRepository, processedStore, outbox, registry);

        inventoryRepository.setStock("item-1", 10);
        inventoryRepository.setStock("item-2", 5);

        List<Item> items = List.of(
                new Item("item-1"),
                new Item("item-1"),
                new Item("item-2"));

        Order order = new Order(
                "order-happy-path",
                OrderState.PENDING,
                PaymentState.PROCESSING,
                InventoryState.NOT_RESERVED,
                items);

        orderRepository.save(order);
        //Act
        Event event = new OrderCreatedEvent(UUID.randomUUID().toString(), order.getId());
        processor.process(event);

        //Assert
        Order result = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderState.CANCELLED, result.getOrderState());
        assertEquals(InventoryState.RELEASED, result.getInventoryState());
    }
}
