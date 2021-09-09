package net.okocraft.box.core.command;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.command.SubCommandHoldable;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.core.message.ErrorMessages;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public abstract class BaseCommand implements Command, SubCommandHoldable, CommandExecutor, TabCompleter, Listener {

    private final SubCommandHolder subCommandHolder = new SubCommandHolder();
    private Command commandOfNoArgument;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission(getPermissionNode())) {
            sender.sendMessage(GeneralMessage.ERROR_NO_PERMISSION.apply(getPermissionNode()));
            return;
        }

        if (args.length == 0) {
            if (commandOfNoArgument != null && sender.hasPermission(commandOfNoArgument.getPermissionNode())) {
                commandOfNoArgument.onCommand(sender, args);
            } else {
                sender.sendMessage(ErrorMessages.ERROR_COMMAND_NO_ARGUMENT);
            }
            return;
        }

        var optionalSubCommand = subCommandHolder.search(args[0]);

        if (optionalSubCommand.isEmpty()) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_SUBCOMMAND_NOT_FOUND);
            return;
        }

        var subCommand = optionalSubCommand.get();

        if (sender.hasPermission(subCommand.getPermissionNode())) {
            CompletableFuture.runAsync(
                    () -> subCommand.onCommand(sender, args),
                    BoxProvider.get().getExecutorProvider().getExecutor()
            ).exceptionallyAsync(e -> reportError(sender, args, e));
        } else {
            sender.sendMessage(GeneralMessage.ERROR_NO_PERMISSION.apply(subCommand.getPermissionNode()));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 0 || !sender.hasPermission(getPermissionNode())) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return subCommandHolder.getSubCommands().stream()
                    .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                    .map(Command::getName)
                    .filter(cmdName -> cmdName.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    .toList();
        }

        return subCommandHolder.search(args[0])
                .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                .map(cmd -> cmd.onTabComplete(sender, args))
                .orElse(Collections.emptyList());
    }

    @Override
    public @NotNull SubCommandHolder getSubCommandHolder() {
        return subCommandHolder;
    }

    public void changeNoArgumentCommand(@NotNull Command command) {
        commandOfNoArgument = command;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        onCommand(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        return onTabComplete(sender, args);
    }

    public void register(@NotNull PluginCommand pluginCommand) {
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }

    @EventHandler
    public void onAsyncTabComplete(@NotNull AsyncTabCompleteEvent event) {
        if (!event.isCommand()) {
            return;
        }

        var buffer = event.getBuffer();

        if (buffer.isEmpty()) {
            return;
        }

        if (buffer.charAt(0) == '/') {
            buffer = buffer.substring(1);
        }

        int firstSpace = buffer.indexOf(' ');
        if (firstSpace < 0) {
            return;
        }

        var label = buffer.substring(0, firstSpace).toLowerCase(Locale.ROOT);

        if (!getName().equals(label) && !getAliases().contains(label)) {
            return;
        }

        String[] args = buffer.substring(firstSpace + 1).split(" ", -1);

        event.setCompletions(onTabComplete(event.getSender(), args));
        event.setHandled(true);
    }

    private Void reportError(@NotNull CommandSender sender, @NotNull String[] args, @NotNull Throwable throwable) {
        sender.sendMessage(ErrorMessages.ERROR_WHILE_EXECUTING_COMMAND.apply(throwable));

        BoxProvider.get().getLogger().log(
                Level.SEVERE,
                "Failed to execute command (/" + getName() + " " + Arrays.toString(args) + ")",
                throwable
        );

        return null;
    }
}
