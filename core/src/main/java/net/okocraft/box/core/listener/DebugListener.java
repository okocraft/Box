package net.okocraft.box.core.listener;

import com.github.siroshun09.event4j.handlerlist.Key;
import com.github.siroshun09.event4j.handlerlist.Priority;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNull;

public class DebugListener {

    private final Key listenerKey = Key.of("box:debug");

    public void register() {
        BoxProvider.get().getEventBus().getHandlerList(BoxEvent.class).subscribe(listenerKey, this::handleEvent, Priority.of(Integer.MIN_VALUE));
    }

    public void unregister() {
        BoxProvider.get().getEventBus().getHandlerList(BoxEvent.class).unsubscribeAll(listenerKey);
    }

    private void handleEvent(@NotNull BoxEvent event) {
        BoxProvider.get().getLogger().info("DEBUG: " + event.toDebugLog());
    }
}
