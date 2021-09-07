package net.okocraft.box.autostore.model;

import com.github.siroshun09.configapi.api.Configuration;
import com.github.siroshun09.configapi.api.MappedConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.autostore.message.AutoStoreMessage;
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
                .thenApplyAsync(this::load)
                .thenAcceptAsync(setting -> settingMap.put(player, setting));
    }

    public @NotNull CompletableFuture<Void> save(@NotNull Player player, @NotNull AutoStoreSetting setting) {
        return CompletableFuture.supplyAsync(() -> save(setting))
                .thenAcceptAsync(data ->
                        BoxProvider.get()
                                .getCustomDataContainer()
                                .set("autostore", player.getUniqueId().toString(), data)
                );
    }

    public @NotNull CompletableFuture<Void> unload(@NotNull Player player) {
        return CompletableFuture.runAsync(() -> {
            var setting = settingMap.remove(player);

            if (setting != null) {
                save(player, setting).join();
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
        for (var entry : settingMap.entrySet()) {
            var player = entry.getKey();
            var setting = entry.getValue();

            save(player, setting).exceptionally(throwable -> {
                BoxProvider.get().getLogger().log(
                        Level.SEVERE,
                        "Could not unload autostore setting (" + player.getName() + ")",
                        throwable
                );

                return null;
            });
        }
    }

    private @NotNull AutoStoreSetting load(@NotNull Configuration data) {
        var setting = new AutoStoreSetting();

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

    private @NotNull Configuration save(@NotNull AutoStoreSetting setting) {
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
