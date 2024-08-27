package net.okocraft.box.feature.autostore.listener;

import dev.siroshun.event4j.api.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.stockholder.StockHolderSaveEvent;
import net.okocraft.box.api.model.stock.PersonalStockHolder;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.SubscribedListenerHolder;
import net.okocraft.box.feature.autostore.AutoStoreSettingProvider;
import net.okocraft.box.feature.autostore.event.AutoStoreSettingChangeEvent;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AutoSaveListener {

    private final AutoStoreSettingProvider container;

    private final Set<AutoStoreSetting> modifiedSettings = ConcurrentHashMap.newKeySet();
    private final SubscribedListenerHolder listenerHolder = new SubscribedListenerHolder();

    public AutoSaveListener(@NotNull AutoStoreSettingProvider container) {
        this.container = container;
    }

    public void register(@NotNull Key listenerKey) {
        this.listenerHolder.subscribeAll(subscriber ->
            subscriber.add(AutoStoreSettingChangeEvent.class, listenerKey, event -> this.modifiedSettings.add(event.getSetting()), Priority.NORMAL)
                .add(StockHolderSaveEvent.class, listenerKey, this::saveModifiedSettings, Priority.NORMAL)
        );
    }

    public void unregister() {
        this.listenerHolder.unsubscribeAll();
    }

    private void saveModifiedSettings(@NotNull StockHolderSaveEvent event) {
        if (event.getStockHolder() instanceof PersonalStockHolder personalStockHolder) {
            var setting = this.container.getIfLoaded(personalStockHolder.getUser().getUUID());

            if (setting != null && this.modifiedSettings.contains(setting)) {
                try {
                    this.container.save(setting);
                } catch (Exception e) {
                    BoxLogger.logger().error("Could not save autostore setting ({})", setting.getUuid(), e);
                }
            }
        }
    }
}
