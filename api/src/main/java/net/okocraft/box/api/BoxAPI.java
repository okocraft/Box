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

/**
 * An API of Box.
 * <p>
 * This interface extends the {@link Plugin},
 * but it is intended for operations that require a plugin instance.
 */
public interface BoxAPI extends Plugin {

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
