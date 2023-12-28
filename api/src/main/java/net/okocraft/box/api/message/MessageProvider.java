package net.okocraft.box.api.message;

import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that provides a localized {@link MiniMessageSource} for {@link CommandSender}s.
 */
public interface MessageProvider {

    /**
     * Gets a localized {@link MiniMessageSource} for {@link CommandSender}s.
     *
     * @param sender a {@link CommandSender}
     * @return a localized {@link MiniMessageSource} or default one
     */
    @NotNull MiniMessageSource findSource(@NotNull CommandSender sender);

}
