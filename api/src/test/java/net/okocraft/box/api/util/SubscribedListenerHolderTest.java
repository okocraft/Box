package net.okocraft.box.api.util;

import dev.siroshun.event4j.api.listener.ListenerSubscriber;
import dev.siroshun.event4j.api.listener.SubscribedListener;
import dev.siroshun.event4j.api.priority.Priority;
import dev.siroshun.event4j.tree.TreeEventService;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

class SubscribedListenerHolderTest {

    private static final Key TEST_KEY = Key.key("box", "test");
    private static final Consumer<BoxEvent> EMPTY_CONSUMER = event -> {
    };

    @Test
    void add() {
        SubscribedListenerHolder holder = new SubscribedListenerHolder();
        ListenerSubscriber<Key, BoxEvent, Priority> subscriber = newSubscriber();

        SubscribedListener<Key, BoxEvent, Priority> subscribed1 = subscriber.subscribe(BoxEvent.class, TEST_KEY, EMPTY_CONSUMER);

        holder.add(subscribed1);
        Assertions.assertEquals(List.of(subscribed1), holder.getListeners());

        SubscribedListener<Key, BoxEvent, Priority> subscribed2 = subscriber.subscribe(BoxEvent.class, TEST_KEY, EMPTY_CONSUMER);

        holder.add(subscribed2);
        Assertions.assertEquals(List.of(subscribed1, subscribed2), holder.getListeners());
    }

    @Test
    void addAll() {
        SubscribedListenerHolder holder = new SubscribedListenerHolder();
        ListenerSubscriber<Key, BoxEvent, Priority> subscriber = newSubscriber();

        SubscribedListener<Key, BoxEvent, Priority> subscribed1 = subscriber.subscribe(BoxEvent.class, TEST_KEY, EMPTY_CONSUMER);
        SubscribedListener<Key, BoxEvent, Priority> subscribed2 = subscriber.subscribe(BoxEvent.class, TEST_KEY, EMPTY_CONSUMER);

        holder.addAll(List.of(subscribed1, subscribed2));
        Assertions.assertEquals(List.of(subscribed1, subscribed2), holder.getListeners());

        holder.addAll(List.of(subscribed2, subscribed1));
        Assertions.assertEquals(List.of(subscribed1, subscribed2, subscribed2, subscribed1), holder.getListeners());
    }

    @Test
    void subscribeAll() {
        SubscribedListenerHolder holder = new SubscribedListenerHolder();
        ListenerSubscriber<Key, BoxEvent, Priority> subscriber = newSubscriber();

        holder.subscribeAll(
            subscriber,
            bulkSubscriber ->
                bulkSubscriber.add(BoxEvent.class, TEST_KEY, EMPTY_CONSUMER)
                    .add(BoxEvent.class, TEST_KEY, EMPTY_CONSUMER)
        );

        Assertions.assertEquals(2, holder.getListeners().size());
        Assertions.assertEquals(2, subscriber.allListeners().size());
    }

    @Test
    void unsubscribeAll() {
        SubscribedListenerHolder holder = new SubscribedListenerHolder();
        ListenerSubscriber<Key, BoxEvent, Priority> subscriber = newSubscriber();

        SubscribedListener<Key, BoxEvent, Priority> subscribed1 = subscriber.subscribe(BoxEvent.class, TEST_KEY, EMPTY_CONSUMER);
        SubscribedListener<Key, BoxEvent, Priority> subscribed2 = subscriber.subscribe(BoxEvent.class, TEST_KEY, EMPTY_CONSUMER);

        holder.addAll(List.of(subscribed1, subscribed2));
        holder.unsubscribeAll0(subscriber);

        Assertions.assertEquals(List.of(), holder.getListeners());
        Assertions.assertEquals(List.of(), subscriber.allListeners());
    }

    private static @NotNull ListenerSubscriber<Key, BoxEvent, Priority> newSubscriber() {
        return TreeEventService.factory()
            .eventClass(BoxEvent.class)
            .keyClass(Key.class)
            .defaultOrder(Priority.NORMAL)
            .create()
            .subscriber();
    }
}
