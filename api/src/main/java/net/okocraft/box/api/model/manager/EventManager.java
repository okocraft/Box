package net.okocraft.box.api.model.manager;

import com.github.siroshun09.event4j.caller.AsyncEventCaller;
import com.github.siroshun09.event4j.listener.ListenerBase;
import com.github.siroshun09.event4j.listener.SubscribedListener;
import com.github.siroshun09.event4j.priority.Priority;
import com.github.siroshun09.event4j.subscriber.EventSubscriber;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * An interface to call events and manage event listeners.
 */
public interface EventManager extends AsyncEventCaller<BoxEvent> {

    /**
     * Calls the event.
     *
     * @param event the event instance
     */
    @Override
    void call(@NotNull BoxEvent event);

    /**
     * Calls {@link #call(BoxEvent)} asynchronously.
     *
     * @param event the event instance
     */
    @Override
    void callAsync(@NotNull BoxEvent event);

    /**
     * Calls {@link #call(BoxEvent)} asynchronously.
     *
     * @param event    the event instance
     * @param callback the {@link Consumer} that is used as a callback after calling the event
     * @param <E>      the event type that inherits from {@link BoxEvent}
     */
    @Override
    <E extends BoxEvent> void callAsync(@NotNull E event, @Nullable Consumer<? super E> callback);

    /**
     * Gets the {@link EventSubscriber} of the specified event.
     *
     * @param eventClass the event to get {@link EventSubscriber}
     * @param <E>        the event type
     * @return the {@link EventSubscriber} of the specified event
     */
    <E extends BoxEvent> @NotNull EventSubscriber<Key, E, Priority> getSubscriber(@NotNull Class<E> eventClass);

    /**
     * Subscribes the given listeners.
     *
     * @param listeners the listeners to subscribe
     * @return {@link SubscribedListener}s
     */
    @NotNull Collection<SubscribedListener<Key, ? extends BoxEvent, Priority>> subscribeAll(@NotNull Iterable<ListenerBase<Key, ? extends BoxEvent, Priority>> listeners);

    /**
     * Unsubscribes the given listeners.
     *
     * @param listeners the listeners to unsubscribe
     */
    void unsubscribeAll(@NotNull Collection<? extends SubscribedListener<Key, ? extends BoxEvent, Priority>> listeners);

    /**
     * Unsubscribes the listeners that subscribed with the specified {@link Key}.
     *
     * @param key the {@link Key} to unsubscribe
     */
    void unsubscribeByKey(@NotNull Key key);

}
