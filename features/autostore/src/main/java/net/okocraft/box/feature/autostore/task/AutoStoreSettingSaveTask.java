package net.okocraft.box.feature.autostore.task;

import com.github.siroshun09.event4j.handlerlist.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.AutoStoreFeature;
import net.okocraft.box.feature.autostore.event.AutoStoreSettingChangeEvent;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class AutoStoreSettingSaveTask implements Runnable {

    private final List<AutoStoreSetting> modifiedSettings = new ArrayList<>();

    @Override
    public void run() {
        var copied = List.copyOf(modifiedSettings);

        modifiedSettings.clear();

        copied.stream()
                .filter(setting -> Bukkit.getPlayer(setting.getUuid()) != null)
                .forEach(setting -> {
                    var task = AutoStoreFeature.container().save(setting);

                    task.exceptionallyAsync(throwable -> {
                        BoxProvider.get().getLogger().log(
                                Level.SEVERE,
                                "Could not unload autostore setting (" + setting.getUuid() + ")",
                                throwable
                        );

                        return null;
                    });
                });
    }

    public void registerListener(@NotNull Key listenerKey) {
        BoxProvider.get()
                .getEventBus()
                .getHandlerList(AutoStoreSettingChangeEvent.class)
                .subscribe(listenerKey, event -> modifiedSettings.add(event.getSetting()));
    }

    public void unregisterListener(@NotNull Key listenerKey) {
        BoxProvider.get()
                .getEventBus()
                .getHandlerList(AutoStoreSettingChangeEvent.class)
                .unsubscribeAll(listenerKey);
    }
}
