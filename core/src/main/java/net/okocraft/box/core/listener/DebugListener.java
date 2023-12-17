package net.okocraft.box.core.listener;

import com.github.siroshun09.event4j.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.model.manager.EventManager;
import net.okocraft.box.api.util.BoxLogger;
import org.jetbrains.annotations.NotNull;

public final class DebugListener {

    private static final Key LISTENER_KEY = Key.key("box", "core/debug_listener");

    public static void register(@NotNull EventManager eventManager) {
        eventManager.getSubscriber(BoxEvent.class).subscribe(LISTENER_KEY, DebugListener::handleEvent, Priority.value(Integer.MIN_VALUE));
    }

    public static void unregister(@NotNull EventManager eventManager) {
        eventManager.getSubscriber(BoxEvent.class).unsubscribeByKey(LISTENER_KEY);
    }

    private static void handleEvent(@NotNull BoxEvent event) {
        BoxLogger.logger().info("DEBUG: {}", event.toDebugLog());
    }
}
