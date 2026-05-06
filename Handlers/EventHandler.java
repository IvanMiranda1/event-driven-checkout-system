package Handlers;

import java.util.List;

import Aggregate.SystemState;
import Events.Event;

public interface EventHandler<E extends Event> {

    boolean canApply(E event, SystemState state);

    List<Event> apply(E event, SystemState state);

}