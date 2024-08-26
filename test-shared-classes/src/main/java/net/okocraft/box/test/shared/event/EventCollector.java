package net.okocraft.box.test.shared.event;

import dev.siroshun.event4j.api.caller.EventCaller;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.caller.EventCallerProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class EventCollector implements EventCallerProvider {

    private final ConcurrentLinkedQueue<BoxEvent> calledEvents = new ConcurrentLinkedQueue<>();
    private final EventCaller<BoxEvent> caller = this.calledEvents::add;

    @Override
    public @NotNull EventCaller<BoxEvent> sync() {
        return this.caller;
    }

    @Override
    public @NotNull EventCaller<BoxEvent> async() {
        return this.caller;
    }

    public <E extends BoxEvent> void checkEvent(@NotNull Class<E> eventClass, @NotNull Consumer<E> checker) {
        checker.accept(Assertions.assertInstanceOf(eventClass, this.nextEvent()));
    }

    public void checkNoEvent() {
        var event = this.calledEvents.poll();
        Assertions.assertNull(event);
    }

    protected @NotNull BoxEvent nextEvent() {
        var event = this.calledEvents.poll();
        Assertions.assertNotNull(event);
        return event;
    }
}
