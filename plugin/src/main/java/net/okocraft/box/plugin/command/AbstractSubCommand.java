package net.okocraft.box.plugin.command;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.BoxPermission;
import net.okocraft.box.plugin.locale.formatter.FormattedMessage;
import net.okocraft.box.plugin.locale.message.Message;
import net.okocraft.box.plugin.result.CommandResult;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

public abstract class AbstractSubCommand implements Command {

    protected final Command parent;

    private final String command;
    private final List<String> aliases;
    private final BoxPermission permission;
    private final Message usage;

    public AbstractSubCommand(@NotNull Command parent,
                              @NotNull String command,
                              @NotNull List<String> aliases,
                              @NotNull BoxPermission permission,
                              @NotNull Message usage) {
        this.parent = parent;

        this.command = command;
        this.aliases = aliases;
        this.permission = permission;
        this.usage = usage;
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

    @Override
    public @NotNull @Unmodifiable List<Command> getSubCommands() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public BoxPermission getPermission() {
        return permission;
    }

    @Override
    public @NotNull FormattedMessage getUsage() {
        return getPlugin().getLocaleLoader().format(usage, true, parent.getCommand(), getCommand());
    }

    @Override
    public @NotNull CommandResult execute(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        if (permission.has(sender)) {
            return onCommand(sender, args);
        } else {
            getPlugin().getLocaleLoader().format(Message.ERROR_NO_PERMISSION, true, permission.getNode());
            return CommandResult.NO_PERMISSION;
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        if (permission.has(sender)) {
            return onTabComplete(sender, args);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public @NotNull Box getPlugin() {
        return parent.getPlugin();
    }

    @NotNull
    protected abstract CommandResult onCommand(@NotNull CommandSender sender, @NotNull ArgumentList args);

    @NotNull
    protected abstract List<String> onTabComplete(@NotNull CommandSender sender, @NotNull ArgumentList args);
}
