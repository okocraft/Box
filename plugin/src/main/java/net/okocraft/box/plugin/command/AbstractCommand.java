package net.okocraft.box.plugin.command;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.BoxPermission;
import net.okocraft.box.plugin.locale.formatter.FormattedMessage;
import net.okocraft.box.plugin.locale.message.Message;
import net.okocraft.box.plugin.result.CommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractCommand implements Command {

    protected final Box plugin;

    private final String command;
    private final List<String> aliases;
    private final List<Command> subCommands;
    private final BoxPermission permission;

    public AbstractCommand(@NotNull Box plugin,
                           @NotNull String command,
                           @NotNull List<String> aliases,
                           @NotNull List<Command> subCommands,
                           @NotNull BoxPermission permission) {
        this.plugin = plugin;

        this.command = command;
        this.aliases = aliases;
        this.subCommands = subCommands;
        this.permission = permission;
    }

    @NotNull
    @Override
    public String getCommand() {
        return command;
    }

    @NotNull
    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @NotNull
    @Override
    public List<Command> getSubCommands() {
        return subCommands;
    }

    @NotNull
    @Override
    public BoxPermission getPermission() {
        return permission;
    }

    @NotNull
    @Override
    public FormattedMessage getUsage() {
        return plugin.getLocaleLoader().format(
                Message.USAGE_COMMAND,
                false,
                command,
                subCommands.stream().map(Command::getCommand).collect(Collectors.joining(", "))
        );
    }

    @Override
    public @NotNull CommandResult execute(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        if (!getPermission().has(sender)) {
            plugin.getLocaleLoader().format(Message.ERROR_NO_PERMISSION, true, getPermission().getNode()).send(sender);
            return CommandResult.NO_PERMISSION;
        }

        if (args.hasElement(0)) {
            Optional<Command> sub = getSubCommand(args.get(0));

            if (sub.isPresent()) {
                return sub.get().execute(sender, args);
            } else {
                plugin.getLocaleLoader().format(Message.ERROR_COMMAND_NOT_FOUND, true).send(sender);
                plugin.getLocaleLoader().format(Message.AVAILABLE_COMMANDS_VIEW, true, getCommand()).send(sender);
                return CommandResult.INVALID_ARGUMENTS;
            }

        } else {
            plugin.getLocaleLoader().format(Message.VERSION, true, plugin.getDescription().getVersion()).send(sender);
            plugin.getLocaleLoader().format(Message.AVAILABLE_COMMANDS_VIEW, true, getCommand()).send(sender);
            return CommandResult.NO_ARGUMENT;
        }
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        if (getPermission().has(sender)) {

            if (args.hasElement(0)) {
                return getSubCommand(args.get(0))
                        .map(command -> command.tabComplete(sender, args))
                        .orElse(Collections.emptyList());
            } else {
                return getSubCommands().stream().map(Command::getCommand).collect(Collectors.toList());
            }

        } else {
            return Collections.emptyList();
        }
    }
}
