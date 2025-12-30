package net.okocraft.box.core.command;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.command.SubCommandHoldable;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.core.message.CoreMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public abstract class BaseCommand implements Command, SubCommandHoldable, Listener {

    private final SubCommandHolder subCommandHolder = new SubCommandHolder();
    private final BoxScheduler scheduler;
    private Command commandOfNoArgument;

    protected BaseCommand(@NotNull BoxScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(args);

        if (!sender.hasPermission(this.getPermissionNode())) {
            sender.sendMessage(ErrorMessages.NO_PERMISSION.apply(this.getPermissionNode()));
            return;
        }

        if (args.length == 0) {
            if (this.commandOfNoArgument != null && sender.hasPermission(this.commandOfNoArgument.getPermissionNode())) {
                this.scheduler.runAsyncTask(() -> this.commandOfNoArgument.onCommand(sender, args));
            } else {
                sender.sendMessage(ErrorMessages.NOT_ENOUGH_ARGUMENT);
                this.sendHelp(sender);
            }
            return;
        }

        Optional<Command> optionalSubCommand = this.subCommandHolder.search(args[0]);

        if (optionalSubCommand.isEmpty()) {
            if (!args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ErrorMessages.SUB_COMMAND_NOT_FOUND);
            }
            this.sendHelp(sender);
            return;
        }

        Command subCommand = optionalSubCommand.get();

        if (sender.hasPermission(subCommand.getPermissionNode())) {
            this.scheduler.runAsyncTask(() -> {
                try {
                    subCommand.onCommand(sender, args);
                } catch (Throwable e) {
                    sender.sendMessage(CoreMessages.COMMAND_EXECUTION_ERROR_MSG.apply(e));
                }
            });
        } else {
            sender.sendMessage(ErrorMessages.NO_PERMISSION.apply(subCommand.getPermissionNode()));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(args);

        if (args.length == 0 || !sender.hasPermission(this.getPermissionNode())) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return this.subCommandHolder.getSubCommands().stream()
                .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                .map(Command::getName)
                .filter(cmdName -> cmdName.startsWith(args[0].toLowerCase(Locale.ROOT)))
                .toList();
        }

        return this.subCommandHolder.search(args[0])
            .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
            .map(cmd -> cmd.onTabComplete(sender, args))
            .orElse(Collections.emptyList());
    }

    @Override
    public @NotNull SubCommandHolder getSubCommandHolder() {
        return this.subCommandHolder;
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return Component.text("/" + this.getName());
    }

    public void changeNoArgumentCommand(@Nullable Command command) {
        this.commandOfNoArgument = command;
    }

    @EventHandler
    public void onAsyncTabComplete(@NotNull AsyncTabCompleteEvent event) {
        if (!event.isCommand()) {
            return;
        }

        String buffer = event.getBuffer();

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

        String label = buffer.substring(0, firstSpace).toLowerCase(Locale.ROOT);

        if (!this.getName().equals(label) && !this.getAliases().contains(label)) {
            return;
        }

        String[] args = buffer.substring(firstSpace + 1).split(" ", -1);

        event.setCompletions(this.onTabComplete(event.getSender(), args));
        event.setHandled(true);
    }

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(CoreMessages.COMMAND_HELP_HEADER.apply("/" + this.getName()));
        this.subCommandHolder.getSubCommands()
            .stream()
            .map(Command::getHelp)
            .forEach(sender::sendMessage);
    }
}
