package net.okocraft.box.feature.autostore;

import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * An interface to load/save {@link AutoStoreSetting}s.
 */
public interface AutoStoreSettingProvider {

    /**
     * Checks if the {@link AutoStoreSetting} of the specified {@link Player} is loaded.
     *
     * @param uuid the {@link Player}'s {@link UUID} to check
     * @return if {@code true}, the {@link Player}'s {@link AutoStoreSetting} is loaded, or if {@code false}, it is not loaded
     */
    boolean isLoaded(@NotNull UUID uuid);

    /**
     * Gets the player's {@link AutoStoreSetting} if loaded.
     *
     * @param uuid the player's {@link UUID}
     * @return the player's {@link AutoStoreSetting}
     */
    @Nullable AutoStoreSetting getIfLoaded(@NotNull UUID uuid);

    /**
     * Gets or load the player's {@link AutoStoreSetting}.
     *
     * @param uuid the player's {@link UUID}
     * @return the player's {@link AutoStoreSetting}
     * @throws Exception if the error happened while loading {@link AutoStoreSetting}
     */
    @NotNull AutoStoreSetting getOrLoad(@NotNull UUID uuid) throws Exception;

    /**
     * Saves the {@link AutoStoreSetting}.
     *
     * @param setting the {@link AutoStoreSetting} to save
     * @throws Exception if the error happened while saving {@link AutoStoreSetting}
     */
    void save(@NotNull AutoStoreSetting setting) throws Exception;

}
