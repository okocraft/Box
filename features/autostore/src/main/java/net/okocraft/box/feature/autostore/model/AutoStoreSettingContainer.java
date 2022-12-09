package net.okocraft.box.feature.autostore.model;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * A class to manage user's {@link AutoStoreSetting}s.
 */
public class AutoStoreSettingContainer {

    /**
     * The instance of {@link AutoStoreSetting}.
     */
    public static final AutoStoreSettingContainer INSTANCE = new AutoStoreSettingContainer();

    private final Map<UUID, AutoStoreSetting> settingMap = new ConcurrentHashMap<>();

    /**
     * Checks if the {@link AutoStoreSetting} of the specified {@link Player} is loaded.
     *
     * @param player the {@link Player} to check
     * @return if {@code true}, the {@link Player}'s {@link AutoStoreSetting} is loaded, or if {@code false}, it is not loaded
     */
    public boolean isLoaded(@NotNull Player player) {
        return settingMap.containsKey(player.getUniqueId());
    }

    /**
     * Gets the {@link AutoStoreSetting} of the specified {@link Player}.
     * <p>
     * Before calling this method, you should check that the {@link Player}'s {@link AutoStoreSetting} have been loaded using {@link #isLoaded(Player)}.
     *
     * @param player the {@link Player} to get the {@link AutoStoreSetting}
     * @return the {@link Player}'s {@link AutoStoreSetting}
     * @throws IllegalStateException if the {@link Player}'s {@link AutoStoreSetting} is not loaded
     */
    public @NotNull AutoStoreSetting get(@NotNull Player player) {
        return Optional.ofNullable(settingMap.get(player.getUniqueId()))
                .orElseThrow(() -> new IllegalStateException("player is not loaded (" + player.getName() + ")"));
    }

    /**
     * Loads the {@link AutoStoreSetting} of the specified {@link Player}.
     * <p>
     * NOTE: <b>This method is for internal use only.</b>
     *
     * @param player the {@link Player} to load the {@link AutoStoreSetting}
     * @return the {@link CompletableFuture} to load the {@link AutoStoreSetting}
     */
    @ApiStatus.Internal
    public @NotNull CompletableFuture<Void> load(@NotNull Player player) {
        return load(player.getUniqueId())
                .thenAcceptAsync(setting -> settingMap.put(player.getUniqueId(), setting));
    }

    /**
     * Loads the {@link AutoStoreSetting} of the specified {@link UUID}.
     * <p>
     * If the player with the specified {@link UUID} is online, use {@link #get(Player)}.
     * The {@link AutoStoreSetting} returned by this method is NOT the same as that of {@link #get(Player)}.
     * To check if the player is online, use {@link #isLoaded(Player)}.
     *
     * @param uuid the {@link UUID} to load the {@link AutoStoreSetting}
     * @return the {@link CompletableFuture} to load the {@link AutoStoreSetting}
     */
    public @NotNull CompletableFuture<AutoStoreSetting> load(@NotNull UUID uuid) {
        return BoxProvider.get()
                .getCustomDataContainer()
                .get("autostore", uuid.toString())
                .thenApplyAsync(data -> AutoStoreSettingSerializer.deserializeConfiguration(uuid, data));
    }

    /**
     * Saves the {@link AutoStoreSetting}.
     *
     * @param setting the {@link AutoStoreSetting} to save
     * @return the {@link CompletableFuture} to save the {@link AutoStoreSetting}
     */
    public @NotNull CompletableFuture<Void> save(@NotNull AutoStoreSetting setting) {
        return CompletableFuture
                .supplyAsync(() -> AutoStoreSettingSerializer.serialize(setting))
                .thenAcceptAsync(
                        data -> BoxProvider.get().getCustomDataContainer()
                                .set("autostore", setting.getUuid().toString(), data)
                );
    }

    /**
     * Unloads the {@link AutoStoreSetting} of the specified {@link Player}.
     * <p>
     * NOTE: <b>This method is for internal use only.</b>
     *
     * @param player the {@link Player} to unload the {@link AutoStoreSetting}
     * @return the {@link CompletableFuture} to unload the {@link AutoStoreSetting}
     */
    @ApiStatus.Internal
    public @NotNull CompletableFuture<Void> unload(@NotNull Player player) {
        return CompletableFuture.runAsync(() -> {
            var setting = settingMap.remove(player.getUniqueId());

            if (setting != null) {
                save(setting).join();
            }
        });
    }

    /**
     * Loads the {@link AutoStoreSetting}s of the online players.
     * <p>
     * NOTE: <b>This method is for internal use only.</b>
     */
    @ApiStatus.Internal
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

    /**
     * Unloads all of the {@link AutoStoreSetting}s.
     * <p>
     * NOTE: <b>This method is for internal use only.</b>
     */
    @ApiStatus.Internal
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
