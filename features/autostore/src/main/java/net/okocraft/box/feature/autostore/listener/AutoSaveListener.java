package net.okocraft.box.feature.autostore.listener;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.event.general.AutoSaveStartEvent;
import net.okocraft.box.feature.autostore.event.AutoStoreSettingChangeEvent;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class AutoSaveListener {

    private final List<AutoStoreSetting> modifiedSettings = new ArrayList<>();

    private Key listenerKey;

    public void register(@NotNull Key listenerKey) {
        this.listenerKey = listenerKey;

        var eventBus = BoxProvider.get().getEventBus();

        eventBus.getHandlerList(AutoStoreSettingChangeEvent.class)
                .subscribe(listenerKey, event -> modifiedSettings.add(event.getSetting()));
        eventBus.getHandlerList(AutoSaveStartEvent.class).subscribe(listenerKey, this::saveModifiedSettings);
    }

    public void unregister() {
        if (listenerKey != null) {
            var eventBus = BoxProvider.get().getEventBus();
            eventBus.getHandlerList(AutoStoreSettingChangeEvent.class).unsubscribeAll(listenerKey);
            eventBus.getHandlerList(AutoSaveStartEvent.class).unsubscribeAll(listenerKey);
        }
    }

    private void saveModifiedSettings(@NotNull AutoSaveStartEvent event) {
        var copied = List.copyOf(modifiedSettings);

        modifiedSettings.clear();

        copied.stream()
                .filter(setting -> Bukkit.getPlayer(setting.getUuid()) != null)
                .forEach(setting -> {
                    var task = AutoStoreSettingContainer.INSTANCE.save(setting);

                    task.exceptionallyAsync(throwable -> {
                        BoxProvider.get().getLogger().log(
                                Level.SEVERE,
                                "Could not save autostore setting (" + setting.getUuid() + ")",
                                throwable
                        );
                        return null;
                    });
                });
    }
}
