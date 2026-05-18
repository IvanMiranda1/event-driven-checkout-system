package Services;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import Aggregate.Order;
import Aggregate.SystemState;
import Events.Event;
import Repositorys.OrderRepository;
import Stores.EventRegistry;
import Stores.Outbox;
import Stores.ProcessedEventStore;
import Handlers.EventHandler;

public class EventProcessor {

    private final OrderRepository orderRepository;
    private final ProcessedEventStore processedEventStore;
    private final Outbox outbox;
    private final EventRegistry eventRegistry;

    public EventProcessor(OrderRepository orderRepository,
            ProcessedEventStore processedEventStore,
            Outbox outbox,
            EventRegistry eventRegistry) {
        this.orderRepository = orderRepository;
        this.processedEventStore = processedEventStore;
        this.outbox = outbox;
        this.eventRegistry = eventRegistry;
    }

    public void process(Event initialEvent) {

        Queue<Event> queue = new LinkedList<>();
        queue.add(initialEvent);

        while (!queue.isEmpty()) {

            Event event = queue.poll();
            
            System.out.println("Processing event: " + event.getClass().getSimpleName());

            List<EventHandler<?>> handlers = eventRegistry.findHandlersFor(event);

            for (EventHandler handler : handlers) {
                //logs
                System.out.println("Handler: " + handler.getClass().getSimpleName());

                String handlerName = handler.getClass().getName();
                String eventId = event.getEventId();

                if (processedEventStore.exists(eventId, handlerName)) {
                    continue;
                }

                Order order = orderRepository.findById(event.getOrderId())
                        .orElseThrow();

                SystemState state = buildState(order);

                if (!handler.canApply(event, state)) {
                    continue;
                }

                try {
                    List<Event> newEvents = handler.apply(event, state);

                    orderRepository.save(order);

                    outbox.save(newEvents);

                    processedEventStore.markAsProcessed(eventId, handlerName);

                    queue.addAll(newEvents);

                    processedEventStore.markAsProcessed(eventId, handlerName);

                } catch (Exception e) {
                    // NO marcar como procesado
                    // retry después
                }
            }

        }

    }

    private SystemState buildState(Order order) {
        return new SystemState(
                order.getId(),
                order.getOrderState(),
                order.getPaymentState(),
                order.getInventoryState());
    }
}

/*
 * process(event):
 * 
 * handlers = findHandlers(event)
 * 
 * for handler in handlers:
 * 
 * if processedEventStore.exists(eventId, handler):
 * continue
 * 
 * if handler.canApply(...):
 * 
 * try:
 * newEvents = handler.apply(...)
 * 
 * save aggregate
 * save events (outbox)
 * 
 * processedEventStore.markAsProcessed(eventId, handler)
 * 
 * catch:
 * → NO marcar
 * → retry después
 * 
 */