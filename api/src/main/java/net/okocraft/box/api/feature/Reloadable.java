package net.okocraft.box.api.feature;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to indicate that it is reloadable.
 */
public interface Reloadable {

    /**
     * Reloads this.
     *
     * @param sender the sender who executed reload
     * @deprecated use/override {@link #reload(FeatureContext.Reloading)}
     */
    @Deprecated(since = "5.5.2", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0.0")
    default void reload(@NotNull CommandSender sender) {
    }

    /**
     * Reloads the {@link BoxFeature}.
     *
     * @param context the {@link net.okocraft.box.api.feature.FeatureContext.Reloading} context
     * @throws Throwable if an exception occurred while reloading
     */
    default void reload(@NotNull FeatureContext.Reloading context) throws Throwable {
        this.reload(context.commandSender());
    }

}
