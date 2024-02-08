package net.okocraft.box.test.shared.event;

import com.github.siroshun09.event4j.caller.AsyncEventCaller;
import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class EventCollector implements AsyncEventCaller<BoxEvent> {

    private final ConcurrentLinkedQueue<BoxEvent> calledEvents = new ConcurrentLinkedQueue<>();

    @Override
    public <T extends BoxEvent> void call(@NotNull T event) {
        this.calledEvents.add(event);
    }

    @Override
    public <T extends BoxEvent> void callAsync(@NotNull T event) {
        this.calledEvents.add(event);
    }

    @Override
    public <T extends BoxEvent> void callAsync(@NotNull T event, @Nullable Consumer<? super T> consumer) {
        this.calledEvents.add(event);

        if (consumer != null) {
            consumer.accept(event);
        }
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
