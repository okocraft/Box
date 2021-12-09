package net.okocraft.box.core.listener;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNull;

public class DebugListener {

    private final Key listenerKey = Key.of("box:debug");

    public void register() {
        BoxProvider.get().getEventBus().getHandlerList(BoxEvent.class).subscribe(listenerKey, this::handleEvent);
    }

    public void unregister() {
        BoxProvider.get().getEventBus().getHandlerList(BoxEvent.class).unsubscribeAll(listenerKey);
    }

    private void handleEvent(@NotNull BoxEvent event) {
        BoxProvider.get().getLogger().info("DEBUG: " + event.toDebugLog());
    }
}
