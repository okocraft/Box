package net.okocraft.box.feature.autostore.model.container;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class AutoStoreSettingContainer {

    private final Map<UUID, AutoStoreSetting> settingMap = new HashMap<>();

    public @NotNull AutoStoreSetting get(@NotNull Player player) {
        return Optional.ofNullable(settingMap.get(player.getUniqueId()))
                .orElseThrow(() -> new IllegalStateException("player is not loaded (" + player.getName() + ")"));
    }

    public @NotNull CompletableFuture<Void> load(@NotNull Player player) {
        return load(player.getUniqueId())
                .thenAcceptAsync(setting -> settingMap.put(player.getUniqueId(), setting));
    }

    public @NotNull CompletableFuture<AutoStoreSetting> load(@NotNull UUID uuid) {
        return BoxProvider.get()
                .getCustomDataContainer()
                .get("autostore", uuid.toString())
                .thenApplyAsync(data -> AutoStoreSettingSerializer.deserializeConfiguration(uuid, data));
    }

    public @NotNull CompletableFuture<Void> save(@NotNull AutoStoreSetting setting) {
        return CompletableFuture
                .supplyAsync(() -> AutoStoreSettingSerializer.serialize(setting))
                .thenAcceptAsync(
                        data -> BoxProvider.get().getCustomDataContainer()
                                .set("autostore", setting.getUuid().toString(), data)
                );
    }

    public @NotNull CompletableFuture<Void> unload(@NotNull Player player) {
        return CompletableFuture.runAsync(() -> {
            var setting = settingMap.remove(player.getUniqueId());

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
        for (var setting : settingMap.values()) {
            save(setting).exceptionally(throwable -> {
                BoxProvider.get().getLogger().log(
                        Level.SEVERE,
                        "Could not unload autostore setting (" + setting.getUuid() + ")",
                        throwable
                );

                return null;
            });
        }
    }
}
