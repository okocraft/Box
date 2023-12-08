package net.okocraft.box.feature.autostore.model;

import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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
        var setting = this.settingMap.get(player.getUniqueId());

        if (setting == null) {
            throw new IllegalStateException("player is not loaded (" + player.getName() + ")");
        }

        return setting;
    }

    /**
     * Loads the {@link AutoStoreSetting} of the specified {@link Player}.
     * <p>
     * NOTE: <b>This method is for internal use only.</b>
     *
     * @param player the {@link Player} to load the {@link AutoStoreSetting}
     */
    @ApiStatus.Internal
    public void load(@NotNull Player player) throws Exception {
        this.settingMap.put(player.getUniqueId(), this.load(player.getUniqueId()));
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
    public @NotNull AutoStoreSetting load(@NotNull UUID uuid) throws Exception {
        var data = BoxProvider.get().getCustomDataManager().loadData(createKey(uuid));
        return AutoStoreSettingSerializer.deserialize(uuid, data);
    }

    /**
     * Saves the {@link AutoStoreSetting}.
     *
     * @param setting the {@link AutoStoreSetting} to save
     */
    public void save(@NotNull AutoStoreSetting setting) throws Exception {
        var data = AutoStoreSettingSerializer.serialize(setting);
        BoxProvider.get().getCustomDataManager().saveData(createKey(setting.getUuid()), data);
    }

    /**
     * Unloads the {@link AutoStoreSetting} of the specified {@link Player}.
     * <p>
     * NOTE: <b>This method is for internal use only.</b>
     *
     * @param player the {@link Player} to unload the {@link AutoStoreSetting}
     */
    @ApiStatus.Internal
    public void unload(@NotNull Player player) throws Exception {
        var setting = this.settingMap.remove(player.getUniqueId());

        if (setting != null) {
            this.save(setting);
        }
    }

    /**
     * Loads the {@link AutoStoreSetting}s of the online players.
     * <p>
     * NOTE: <b>This method is for internal use only.</b>
     */
    @ApiStatus.Internal
    public void loadAll() {
        for (var player : Bukkit.getOnlinePlayers()) {
            try {
                this.load(player);
            } catch (Exception e) {
                BoxLogger.logger().error("Could not load autostore setting ({})", player.getName(), e);
                player.sendMessage(AutoStoreMessage.ERROR_FAILED_TO_LOAD_SETTINGS);
            }
        }
    }

    /**
     * Unloads all of the {@link AutoStoreSetting}s.
     * <p>
     * NOTE: <b>This method is for internal use only.</b>
     */
    @ApiStatus.Internal
    public void unloadAll() {
        for (var setting : this.settingMap.values()) {
            try {
                this.save(setting);
            } catch (Exception e) {
                BoxLogger.logger().error("Could not unload autostore setting ({})", setting.getUuid(), e);
            }
        }
    }

    @SuppressWarnings("PatternValidation")
    private static @NotNull Key createKey(@NotNull UUID uuid) {
        return Key.key("autostore", uuid.toString());
    }
}
