package net.okocraft.box.api.util;

import dev.siroshun.event4j.api.listener.ListenerSubscriber;
import dev.siroshun.event4j.api.listener.SubscribedListener;
import dev.siroshun.event4j.api.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A utility class for holding {@link SubscribedListener}s.
 * <p>
 * This can be used after {@link BoxAPI} is initialized.
 */
public final class SubscribedListenerHolder {

    private final List<SubscribedListener<Key, ? extends BoxEvent, Priority>> listeners = new ArrayList<>();

    /**
     * Adds a {@link SubscribedListener}.
     *
     * @param listener a {@link SubscribedListener}
     */
    public void add(@NotNull SubscribedListener<Key, ? extends BoxEvent, Priority> listener) {
        this.listeners.add(listener);
    }

    /**
     * Adds the {@link SubscribedListener}s.
     *
     * @param listeners the {@link SubscribedListener}s
     */
    public void addAll(@NotNull List<SubscribedListener<Key, ? extends BoxEvent, Priority>> listeners) {
        this.listeners.addAll(listeners);
    }

    /**
     * Gets the {@link SubscribedListener}s this instance holds.
     *
     * @return the {@link SubscribedListener}s this instance holds
     */
    @Contract(pure = true)
    public @NotNull @UnmodifiableView List<SubscribedListener<Key, ? extends BoxEvent, Priority>> getListeners() {
        return Collections.unmodifiableList(this.listeners);
    }

    /**
     * Subscribes listeners to {@link BoxAPI#getListenerSubscriber()} using {@link dev.siroshun.event4j.api.listener.ListenerSubscriber.BulkSubscriber}
     * and holds {@link SubscribedListener}s it returns.
     *
     * @param operator the {@link UnaryOperator} to modify {@link dev.siroshun.event4j.api.listener.ListenerSubscriber.BulkSubscriber}
     */
    public void subscribeAll(@NotNull UnaryOperator<ListenerSubscriber.BulkSubscriber<Key, BoxEvent, Priority>> operator) {
        this.subscribeAll(BoxAPI.api().getListenerSubscriber(), operator);
    }

    @VisibleForTesting
    void subscribeAll(@NotNull ListenerSubscriber<Key, BoxEvent, Priority> subscriber, @NotNull UnaryOperator<ListenerSubscriber.BulkSubscriber<Key, BoxEvent, Priority>> operator) {
        this.listeners.addAll(operator.apply(subscriber.bulkSubscriber()).subscribe());
    }

    /**
     * Unsubscribes {@link SubscribedListener}s this instance holds.
     */
    public void unsubscribeAll() {
        this.unsubscribeAll0(BoxAPI.api().getListenerSubscriber());
    }

    @VisibleForTesting
    void unsubscribeAll0(@NotNull ListenerSubscriber<Key, BoxEvent, Priority> subscriber) {
        subscriber.unsubscribeAll(this.listeners);
        this.listeners.clear();
    }
}
