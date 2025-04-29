package net.okocraft.box.feature.autostore.listener;

import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.customdata.CustomDataExportEvent;
import net.okocraft.box.api.util.SubscribedListenerHolder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CustomDataExportListener {

    private final SubscribedListenerHolder listenerHolder = new SubscribedListenerHolder();

    public void register(@NotNull Key listenerKey, @NotNull Consumer<CustomDataExportEvent> consumer) {
        this.listenerHolder.subscribeAll(subscriber ->
            subscriber.add(CustomDataExportEvent.class, listenerKey, consumer)
        );
    }

    public void unregister() {
        this.listenerHolder.unsubscribeAll();
    }
}
