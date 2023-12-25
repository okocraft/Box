package net.okocraft.box.feature.autostore.listener;

import com.github.siroshun09.event4j.listener.ListenerBase;
import com.github.siroshun09.event4j.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.event.general.AutoSaveStartEvent;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.autostore.event.AutoStoreSettingChangeEvent;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AutoSaveListener {

    private final Set<AutoStoreSetting> modifiedSettings = ConcurrentHashMap.newKeySet();
    private final AutoStoreSettingContainer container;

    public AutoSaveListener(@NotNull AutoStoreSettingContainer container) {
        this.container = container;
    }

    public void register(@NotNull Key listenerKey) {
        BoxAPI.api().getEventManager().subscribeAll(List.of(
                new ListenerBase<>(AutoStoreSettingChangeEvent.class, listenerKey, event -> this.modifiedSettings.add(event.getSetting()), Priority.NORMAL),
                new ListenerBase<>(AutoSaveStartEvent.class, listenerKey, event -> this.saveModifiedSettings(), Priority.NORMAL)
        ));
    }

    public void unregister(@NotNull Key listenerKey) {
        BoxAPI.api().getEventManager().unsubscribeByKey(listenerKey);
    }

    private void saveModifiedSettings() {
        var copied = List.copyOf(modifiedSettings);
        modifiedSettings.clear();

        copied.stream()
                .filter(setting -> Bukkit.getPlayer(setting.getUuid()) != null)
                .forEach(setting -> {
                    try {
                        this.container.save(setting);
                    } catch (Exception e) {
                        BoxLogger.logger().error("Could not save autostore setting ({})", setting.getUuid(), e);
                    }
                });
    }
}
