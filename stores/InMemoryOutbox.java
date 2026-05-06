package stores;

import java.util.ArrayList;
import java.util.List;

import Events.Event;

public class InMemoryOutbox implements Outbox{
    
    private final List<Event> events = new ArrayList<>();

    @Override
    public void save(List<Event> newEvents){
        events.addAll(newEvents);
    }

    public List<Event> getAll(){
        return events;
    }
}
