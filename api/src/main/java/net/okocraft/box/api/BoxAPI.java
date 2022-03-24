package net.okocraft.box.api;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.event4j.bus.EventBus;
import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.model.data.CustomDataContainer;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.taskfactory.TaskFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * An API of Box.
 */
public interface BoxAPI {

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
     */
    @NotNull Path getJar();

    /**
     * Gets the {@link Logger}.
     *
     * @return the {@link Logger}
     */
    @NotNull Logger getLogger();

    /**
     * Gets the {@link YamlConfiguration} that loaded from config.yml.
     *
     * @return the {@link YamlConfiguration}
     */
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
     * Gets the {@link EventBus}.
     *
     * @return the {@link EventBus}
     */
    @NotNull EventBus<BoxEvent> getEventBus();

    /**
     * Gets the {@link CustomDataContainer}.
     *
     * @return the {@link CustomDataContainer}
     */
    @NotNull CustomDataContainer getCustomDataContainer();

    /**
     * Gets the {@link TaskFactory}.
     *
     * @return the {@link TaskFactory}
     */
    @NotNull TaskFactory getTaskFactory();

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
     */
    @NotNull @Unmodifiable List<BoxFeature> getFeatures();

    /**
     * Registers the {@link BoxFeature}.
     *
     * @param boxFeature the {@link BoxFeature} to register
     */
    void register(@NotNull BoxFeature boxFeature);

    /**
     * Unregisters the {@link BoxFeature}.
     *
     * @param boxFeature the {@link BoxFeature} to unregister
     */
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
     * Checks if Box is not available in the world.
     * <p>
     * Returns false if the player has the {@code box.admin.ignore-disabled-world} permission.
     *
     * @param player the player to check
     * @return if Box is disabled in that world, returns {@code true}, otherwise {@code false}
     */
    boolean isDisabledWorld(@NotNull Player player);

    /**
     * Creates a {@link NamespacedKey}.
     *
     * @param value the value of the {@link NamespacedKey}
     * @return a new {@link NamespacedKey}
     */
    @NotNull NamespacedKey createNamespacedKey(@NotNull String value);
}
