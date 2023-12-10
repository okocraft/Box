package net.okocraft.box.test.shared.event;

import com.github.siroshun09.event4j.bus.EventBus;
import com.github.siroshun09.event4j.bus.EventSubscriber;
import com.github.siroshun09.event4j.bus.PostResult;
import com.github.siroshun09.event4j.bus.SubscribedListener;
import com.github.siroshun09.event4j.key.Key;
import com.github.siroshun09.event4j.listener.MultipleListeners;
import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EventCollector implements EventBus<BoxEvent> {

    private final ConcurrentLinkedQueue<BoxEvent> calledEvents = new ConcurrentLinkedQueue<>();

    @Override
    public @NotNull Class<BoxEvent> getEventClass() {
        return BoxEvent.class;
    }

    @Override
    public @NotNull <T extends BoxEvent> EventSubscriber<T> getSubscriber(@NotNull Class<T> eventClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull @UnmodifiableView Collection<EventSubscriber<?>> getSubscribers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull @UnmodifiableView List<SubscribedListener<?>> subscribeAll(@NotNull Key key, @NotNull MultipleListeners listeners) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends BoxEvent> boolean unsubscribe(@NotNull SubscribedListener<T> subscribedListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unsubscribeAll(@NotNull List<SubscribedListener<?>> subscribedListeners) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unsubscribeAll(@NotNull Key key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unsubscribeIf(@NotNull Predicate<SubscribedListener<?>> predicate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends BoxEvent> @NotNull T callEvent(@NotNull T event) {
        this.calledEvents.add(event);
        return event;
    }

    @Override
    public @NotNull <T extends BoxEvent> CompletableFuture<T> callEventAsync(@NotNull T event) {
        this.calledEvents.add(event);
        return CompletableFuture.completedFuture(event);
    }

    @Override
    public @NotNull <T extends BoxEvent> CompletableFuture<T> callEventAsync(@NotNull T event, @NotNull Executor executor) {
        this.calledEvents.add(event);
        return CompletableFuture.completedFuture(event);
    }

    @Override
    public @NotNull <T extends BoxEvent> CompletableFuture<T> callEventAsync(@NotNull T event, @NotNull Function<Supplier<T>, CompletableFuture<T>> supplierCompletableFutureFunction) {
        this.calledEvents.add(event);
        return CompletableFuture.completedFuture(event);
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addResultConsumer(@NotNull Consumer<@NotNull PostResult<?>> consumer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeResultConsumer(@NotNull Consumer<@NotNull PostResult<?>> consumer) {
        throw new UnsupportedOperationException();
    }

    public <E extends BoxEvent> void checkEvent(@NotNull Class<E> eventClass, @NotNull Consumer<E> checker) {
        checker.accept(Assertions.assertInstanceOf(eventClass, this.calledEvents.poll()));
    }
}
