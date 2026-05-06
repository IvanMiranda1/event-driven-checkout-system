package stores;

import java.util.*;
import Events.Event;
import Handlers.EventHandler;

public class EventRegistry {
    // Aquí es donde se guardan los datos.
    // Key: El tipo de evento (ej. OrderCompletedEvent.class)
    // Value: Una lista con los objetos Handler que lo escuchan

    private final Map<Class<? extends Event>, List<EventHandler<?>>> subscriptions = new HashMap<>();

    // Este es el metodo que registra las "suscripciones"
    public void subscribe(Class<? extends Event> eventType, EventHandler<?> handler) {
        subscriptions.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }

    // Este es el que usa EventProcessor para buscar qué hadlers ejecutar
    public List<EventHandler<?>> findHandlersFor(Event event) {
        return subscriptions.getOrDefault(event.getClass(),  List.of());
    }
}

