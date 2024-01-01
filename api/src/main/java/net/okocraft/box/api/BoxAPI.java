package net.okocraft.box.api;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.event4j.bus.EventBus;
import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureProvider;
import net.okocraft.box.api.model.data.CustomDataContainer;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.api.taskfactory.TaskFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * An API of Box.
 */
public interface BoxAPI {

    /**
     * Gets an instance of {@link BoxAPI}.
     *
     * @return an instance of {@link BoxAPI}
     * @throws IllegalStateException if {@link BoxAPI} is not loaded
     */
    static @NotNull BoxAPI api() {
        return BoxProvider.get();
    }

    /**
     * Checks if {@link BoxAPI} is available.
     *
     * @return {@code true} if {@link BoxAPI} is loaded, otherwise {@code false}
     */
    static boolean isLoaded() {
        return BoxProvider.API != null;
    }

    /**
     * Gets the instance of Box.
     * <p>
     * This is intended for operations that require a plugin instance.
     *
     * @return the plugin instance
     */
    @NotNull Plugin getPluginInstance();

    /**
     * Gets the path of the plugin directory.
     *
     * @return the path of the plugin directory
     */
    @NotNull Path getPluginDirectory();

    /**
     * Gets the path of the plugin jar.
     *
     * @return tha path of the plugin jar
     * @deprecated no replacement
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull Path getJar();

    /**
     * Gets the {@link Logger}.
     *
     * @return the {@link Logger}
     * @deprecated Should use your own {@link Logger}. If not, there is {@link net.okocraft.box.api.util.BoxLogger}, which is marked for internal use.
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull Logger getLogger();

    /**
     * Gets the {@link YamlConfiguration} that loaded from config.yml.
     *
     * @return the {@link YamlConfiguration}
     * @deprecated no replacement
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull YamlConfiguration getConfiguration();

    /**
     * Gets the {@link UserManager}.
     *
     * @return the {@link UserManager}
     */
    @NotNull UserManager getUserManager();

    /**
     * Gets the {@link ItemManager}.
     *
     * @return the {@link ItemManager}
     */
    @NotNull ItemManager getItemManager();

    /**
     * Gets the {@link StockManager}.
     *
     * @return the {@link StockManager}
     */
    @NotNull StockManager getStockManager();

    /**
     * Gets the {@link BoxPlayerMap}.
     *
     * @return the {@link BoxPlayerMap}
     */
    @NotNull BoxPlayerMap getBoxPlayerMap();

    /**
     * Gets the {@link FeatureProvider}.
     *
     * @return the {@link FeatureProvider}
     */
    @NotNull FeatureProvider getFeatureProvider();

    /**
     * Gets the {@link EventBus}.
     *
     * @return the {@link EventBus}
     * @deprecated reworked in Box v6.0.0 (in v5, there is no replacement)
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull EventBus<BoxEvent> getEventBus();

    /**
     * Gets the {@link CustomDataContainer}.
     *
     * @return the {@link CustomDataContainer}
     * @deprecated reworked in Box v6.0.0 (in v5, there is no replacement)
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull CustomDataContainer getCustomDataContainer();

    /**
     * Gets the {@link TaskFactory}.
     *
     * @return the {@link TaskFactory}
     * @deprecated use {@link net.okocraft.box.api.scheduler.BoxScheduler}
     */
    @Deprecated(since = "5.5.0", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull TaskFactory getTaskFactory();

    /**
     * Gets the {@link BoxScheduler}.
     *
     * @return the {@link BoxScheduler}
     */
    @NotNull BoxScheduler getScheduler();

    /**
     * Gets the {@link BoxCommand}.
     *
     * @return the {@link BoxCommand}
     */
    @NotNull BoxCommand getBoxCommand();

    /**
     * Gets the {@link BoxAdminCommand}.
     *
     * @return the {@link BoxAdminCommand}
     */
    @NotNull BoxAdminCommand getBoxAdminCommand();

    /**
     * Gets registered {@link BoxFeature}s.
     *
     * @return registered {@link BoxFeature}
     * @deprecated use {@link FeatureProvider#getFeatures()}}
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull @Unmodifiable List<BoxFeature> getFeatures();

    /**
     * Gets the registered {@link BoxFeature} instance.
     * <p>
     * Note: This method returns the first BoxFeature instance with matching class from {@link #getFeatures()}.
     *
     * @param clazz the feature class
     * @param <T>   the type of {@link BoxFeature}
     * @return if the {@link BoxFeature} is registered, returns its instance, otherwise {@link Optional#empty()}
     * @deprecated use {@link FeatureProvider#getFeature(Class)}}
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    <T extends BoxFeature> @NotNull Optional<T> getFeature(@NotNull Class<T> clazz);

    /**
     * Registers the {@link BoxFeature}.
     *
     * @param boxFeature the {@link BoxFeature} to register
     * @deprecated runtime registration is not supported in v6.0.0
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    void register(@NotNull BoxFeature boxFeature);

    /**
     * Unregisters the {@link BoxFeature}.
     *
     * @param boxFeature the {@link BoxFeature} to unregister
     * @deprecated runtime unregistration is not supported in v6.0.0
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    void unregister(@NotNull BoxFeature boxFeature);

    /**
     * Reloads box and registered {@link BoxFeature}s.
     * <p>
     * This method reloads some settings of Box (such as language and disabled world)
     * and the {@link BoxFeature} in which {@link net.okocraft.box.api.feature.Reloadable} is implemented.
     *
     * @param sender the sender who executed reload
     */
    void reload(@NotNull CommandSender sender);

    /**
     * Checks if the specified {@link Player} can use Box.
     *
     * @param player the {@link Player} to check
     * @return {@code true} if the {@link Player} can use Box, otherwise {@code false}
     */
    boolean canUseBox(@NotNull Player player);

    /**
     * Checks if Box is not available in the world where the {@link Player} is located.
     * <p>
     * Returns false if the player has the {@code box.admin.ignore-disabled-world} permission.
     *
     * @param player the player to check
     * @return if Box is disabled in the world, returns {@code true}, otherwise {@code false}
     * @deprecated use {@link #canUseBox(Player)}
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    boolean isDisabledWorld(@NotNull Player player);

    /**
     * Checks if Box is not available in the specified world.
     *
     * @param world the world to check
     * @return if Box is disabled in the specified world, returns {@code true}, otherwise {@code false}
     */
    boolean isDisabledWorld(@NotNull World world);

    /**
     * Checks if Box is not available in the specified world name.
     *
     * @param worldName the world name to check
     * @return if Box is disabled in the specified world, returns {@code true}, otherwise {@code false}
     */
    boolean isDisabledWorld(@NotNull String worldName);

    /**
     * Creates a {@link NamespacedKey}.
     *
     * @param value the value of the {@link NamespacedKey}
     * @return a new {@link NamespacedKey}
     * @deprecated use {@link NamespacedKey#NamespacedKey(String, String)}}, it is not deprecated in Paper API since 1.19
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    @NotNull NamespacedKey createNamespacedKey(@NotNull String value);
}
