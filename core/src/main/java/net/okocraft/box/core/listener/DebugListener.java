package net.okocraft.box.core.listener;

import com.github.siroshun09.event4j.bus.EventBus;
import com.github.siroshun09.event4j.key.Key;
import com.github.siroshun09.event4j.priority.Priority;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNull;

public final class DebugListener {

    private static final Key LISTENER_KEY = Key.create("box", "debug");

    public static void register(@NotNull EventBus<BoxEvent> eventBus) {
        eventBus.getSubscriber(BoxEvent.class).subscribe(LISTENER_KEY, DebugListener::handleEvent, Priority.value(Integer.MIN_VALUE));
    }

    public static void unregister(@NotNull EventBus<BoxEvent> eventBus) {
        eventBus.getSubscriber(BoxEvent.class).unsubscribeAll(LISTENER_KEY);
    }

    private static void handleEvent(@NotNull BoxEvent event) {
        BoxProvider.get().getLogger().info("DEBUG: " + event.toDebugLog());
    }
}
