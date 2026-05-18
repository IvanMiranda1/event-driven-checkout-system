package Stores;

import java.util.HashSet;
import java.util.Set;

public class InMemoryProcessedEventStore implements ProcessedEventStore{
    
    private final Set<String> processed = new HashSet<>();

    private String key(String eventId, String handlerName){
        return eventId + "::" + handlerName;
    }

    @Override
    public boolean exists(String eventId, String handlerName){
        return processed.contains(key(eventId,handlerName));
    }

    @Override
    public void markAsProcessed(String eventId, String handlerName) {
        processed.add(key(eventId, handlerName));
    }
}
