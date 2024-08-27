package net.okocraft.box.api;

import dev.siroshun.event4j.api.listener.ListenerSubscriber;
import dev.siroshun.event4j.api.priority.Priority;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.caller.EventCallerProvider;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureProvider;
import net.okocraft.box.api.message.MessageProvider;
import net.okocraft.box.api.model.customdata.CustomDataManager;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.scheduler.BoxScheduler;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

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
     * Gets the path of the plugin directory.
     *
     * @return the path of the plugin directory
     */
    @NotNull Path getPluginDirectory();

    /**
     * Gets the {@link MessageProvider}.
     *
     * @return the {@link MessageProvider}
     */
    @NotNull MessageProvider getMessageProvider();

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
     * Gets the {@link EventCallerProvider}.
     *
     * @return the {@link EventCallerProvider}
     */
    @NotNull EventCallerProvider getEventCallers();

    /**
     * Gets the {@link ListenerSubscriber}.
     *
     * @return the {@link ListenerSubscriber}
     */
    @NotNull ListenerSubscriber<Key, BoxEvent, Priority> getListenerSubscriber();

    /**
     * Gets the {@link CustomDataManager}.
     *
     * @return the {@link CustomDataManager}
     */
    @NotNull CustomDataManager getCustomDataManager();

    /**
     * Gets the {@link FeatureProvider}.
     *
     * @return the {@link FeatureProvider}
     */
    @NotNull FeatureProvider getFeatureProvider();

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
}
