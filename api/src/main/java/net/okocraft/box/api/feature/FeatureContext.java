package net.okocraft.box.api.feature;

import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.model.manager.EventManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * A class that define contexts for {@link BoxFeature}.
 */
public final class FeatureContext {

    /**
     * A context on registration.
     *
     * @param dataDirectory           a plugin's data directory
     * @param defaultMessageCollector a {@link DefaultMessageCollector} to collect default messages
     * @param eventManager            a {@link EventManager}
     */
    public record Registration(@NotNull Path dataDirectory, @NotNull DefaultMessageCollector defaultMessageCollector,
                               @NotNull EventManager eventManager) {
    }

    /**
     * A context on enabling.
     *
     * @param plugin a {@link Plugin} instance
     */
    public record Enabling(@NotNull Plugin plugin) {
    }

    /**
     * A context on disabling.
     *
     * @param plugin a {@link Plugin} instance
     */
    public record Disabling(@NotNull Plugin plugin) {
    }

    /**
     * A context on reloading.
     *
     * @param plugin        a {@link Plugin} instance
     * @param commandSender a {@link CommandSender} who requests reloading
     */
    public record Reloading(@NotNull Plugin plugin, @NotNull CommandSender commandSender) {

        /**
         * Converts this context to {@link Enabling}.
         *
         * @return a converted {@link Enabling}
         */
        @Contract(" -> new")
        public @NotNull Enabling asEnabling() {
            return new Enabling(this.plugin);
        }

        /**
         * Converts this context to {@link Disabling}.
         *
         * @return a converted {@link Disabling}
         */
        @Contract(" -> new")
        public @NotNull Disabling asDisabling() {
            return new Disabling(this.plugin);
        }

    }

    private FeatureContext() {
        throw new UnsupportedOperationException();
    }
}
