package net.okocraft.box.feature.autostore.listener;

import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.customdata.CustomDataExportEvent;
import net.okocraft.box.api.event.customdata.DataExportPrepareEvent;
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

    public void register(@NotNull Key listenerKey, @NotNull Consumer<CustomDataExportEvent> consumer,
                         DataExportPrepareEvent.@NotNull Preparation preparation) {
        this.register(listenerKey, consumer);
        this.listenerHolder.subscribeAll(subscriber ->
            subscriber.add(DataExportPrepareEvent.class, listenerKey, event -> event.addPreparation(preparation))
        );
    }

    public void unregister() {
        this.listenerHolder.unsubscribeAll();
    }
}
