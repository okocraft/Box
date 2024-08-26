package net.okocraft.box.core.listener;

import dev.siroshun.event4j.api.listener.ListenerSubscriber;
import dev.siroshun.event4j.api.listener.SubscribedListener;
import dev.siroshun.event4j.api.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.util.BoxLogger;
import org.jetbrains.annotations.NotNull;

public final class DebugListener {

    private static final Key LISTENER_KEY = Key.key("box", "core/debug_listener");

    public static @NotNull SubscribedListener<Key, BoxEvent, Priority> register(@NotNull ListenerSubscriber<Key, BoxEvent, Priority> subscriber) {
        return subscriber.subscribe(BoxEvent.class, LISTENER_KEY, DebugListener::handleEvent, Priority.value(Integer.MIN_VALUE));
    }

    private static void handleEvent(@NotNull BoxEvent event) {
        BoxLogger.logger().info("DEBUG: {}", event.toDebugLog());
    }
}
