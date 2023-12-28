package net.okocraft.box.feature.autostore.command;

import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

abstract class AutoStoreSubCommand {

    private final String name;

    protected AutoStoreSubCommand(@NotNull String name) {
        this.name = name.toLowerCase(Locale.ROOT);
    }

    @NotNull String getName() {
        return this.name;
    }

    abstract void runCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull MiniMessageSource msgSrc, @NotNull AutoStoreSetting setting);

    abstract @NotNull List<String> runTabComplete(@NotNull CommandSender sender, @NotNull String[] args);
}
