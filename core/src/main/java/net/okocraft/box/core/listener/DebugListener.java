package net.okocraft.box.core.listener;

import com.github.siroshun09.event4j.key.Key;
import com.github.siroshun09.event4j.priority.Priority;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNull;

public class DebugListener {

    private final Key listenerKey = Key.create("box:debug");

    public void register() {
        BoxProvider.get().getEventBus()
                .getSubscriber(BoxEvent.class)
                .subscribe(listenerKey, this::handleEvent, Priority.value(Integer.MIN_VALUE));
    }

    public void unregister() {
        BoxProvider.get().getEventBus().getSubscriber(BoxEvent.class).unsubscribeAll(listenerKey);
    }

    private void handleEvent(@NotNull BoxEvent event) {
        BoxProvider.get().getLogger().info("DEBUG: " + event.toDebugLog());
    }
}
