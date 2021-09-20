package net.okocraft.box.core.listener;

import com.github.siroshun09.event4j.handlerlist.Key;
import com.github.siroshun09.event4j.listener.Listener;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.BoxEvent;

public class DebugListener {

    private final Listener<BoxEvent> debugListener = event -> BoxProvider.get().getLogger().info("DEBUG: " + event);

    public void register() {
        BoxProvider.get().getEventBus().getHandlerList(BoxEvent.class).subscribe(Key.of("box:debug"), debugListener);
    }

    public void unregister() {
        BoxProvider.get().getEventBus().getHandlerList(BoxEvent.class).unsubscribe(debugListener);
    }
}
