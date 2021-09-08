package net.okocraft.box.feature.autostore.model;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.MappedConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SettingManager {

    private final Map<Player, AutoStoreSetting> settingMap = new HashMap<>();

    public @NotNull AutoStoreSetting get(@NotNull Player player) {
        return Optional.ofNullable(settingMap.get(player))
                .orElseThrow(() -> new IllegalStateException("player is not loaded (" + player.getName() + ")"));
    }

    public @NotNull CompletableFuture<Void> load(@NotNull Player player) {
        return BoxProvider.get()
                .getCustomDataContainer()
                .get("autostore", player.getUniqueId().toString())
                .thenApplyAsync(data -> toAutoStoreSetting(player, data))
                .thenAcceptAsync(setting -> settingMap.put(player, setting));
    }

    public @NotNull CompletableFuture<Void> save(@NotNull AutoStoreSetting setting) {
        return CompletableFuture.supplyAsync(() -> toConfiguration(setting))
                .thenAcceptAsync(data ->
                        BoxProvider.get()
                                .getCustomDataContainer()
                                .set("autostore", setting.getPlayer().getUniqueId().toString(), data)
                );
    }

    public @NotNull CompletableFuture<Void> unload(@NotNull Player player) {
        return CompletableFuture.runAsync(() -> {
            var setting = settingMap.remove(player);

            if (setting != null) {
                save(setting).join();
            }
        });
    }

    public void loadAll() {
        for (var player : Bukkit.getOnlinePlayers()) {
            load(player).exceptionally(throwable -> {
                BoxProvider.get().getLogger().log(
                        Level.SEVERE,
                        "Could not load autostore setting (" + player.getName() + ")",
                        throwable
                );

                player.sendMessage(AutoStoreMessage.ERROR_FAILED_TO_LOAD_SETTINGS);

                return null;
            });
        }
    }

    public void unloadAll() {
        for (var setting: settingMap.values()) {
            save(setting).exceptionally(throwable -> {
                BoxProvider.get().getLogger().log(
                        Level.SEVERE,
                        "Could not unload autostore setting (" + setting.getPlayer().getName() + ")",
                        throwable
                );

                return null;
            });
        }
    }

    private @NotNull AutoStoreSetting toAutoStoreSetting(@NotNull Player player, @NotNull Configuration data) {
        var setting = new AutoStoreSetting(player);

        var mode = data.getString("mode");

        setting.setMode(
                mode.equals("per-item") ?
                        setting.getPerItemModeSetting() :
                        setting.getAllModeSetting()
        );

        setting.getAllModeSetting().setEnabled(data.getBoolean("all-mode-enabled"));

        var enabledItems =
                data.getIntegerList("per-item-mode-enabled")
                        .stream()
                        .map(BoxProvider.get().getItemManager()::getBoxItem)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        setting.getPerItemModeSetting().setEnabledItems(enabledItems);

        return setting;
    }

    private @NotNull Configuration toConfiguration(@NotNull AutoStoreSetting setting) {
        var data = MappedConfiguration.create();

        data.set("mode", setting.getCurrentMode().getModeName());

        data.set("all-mode-enabled", setting.getAllModeSetting().isEnabled());

        var list =
                setting.getPerItemModeSetting()
                        .getEnabledItems()
                        .stream()
                        .map(BoxItem::getInternalId)
                        .sorted()
                        .toList();

        data.set("per-item-mode-enabled", list);

        return data;
    }
}
