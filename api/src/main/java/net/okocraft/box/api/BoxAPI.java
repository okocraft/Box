package net.okocraft.box.api;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserLoader;
import net.okocraft.box.api.player.BoxPlayerMap;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
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
     * Gets the {@link UserLoader}.
     *
     * @return the {@link UserLoader}
     */
    @NotNull UserLoader getUserLoader();

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
}
