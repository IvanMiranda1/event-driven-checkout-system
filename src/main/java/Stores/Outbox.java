package Stores;

import java.util.List;

import Events.Event;

public interface Outbox {
    void save(List<Event> events);
}
