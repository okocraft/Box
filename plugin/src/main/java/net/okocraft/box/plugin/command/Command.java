package net.okocraft.box.plugin.command;

import net.okocraft.box.plugin.BoxPermission;
import net.okocraft.box.plugin.locale.formatter.FormattedMessage;
import net.okocraft.box.plugin.locale.message.Message;
import net.okocraft.box.plugin.result.CommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

public interface Command {

    @NotNull String getCommand();

    @NotNull @Unmodifiable List<String> getAliases();

    @NotNull @Unmodifiable List<Command> getSubCommands();

    @NotNull
    default Optional<Command> getSubCommand(@NotNull String command) {
        String cmd = command.toLowerCase();

        return getSubCommands().stream()
                .filter(c -> c.getCommand().equalsIgnoreCase(cmd) || c.getAliases().contains(cmd))
                .findFirst();
    }

    @NotNull BoxPermission getPermission();

    @NotNull FormattedMessage getUsage();

    @NotNull CommandResult execute(@NotNull CommandSender sender, @NotNull ArgumentList args);

    @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull ArgumentList args);
}
