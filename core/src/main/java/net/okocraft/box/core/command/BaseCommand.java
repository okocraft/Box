package net.okocraft.box.core.command;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.command.SubCommandHoldable;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.message.MessageProvider;
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

public abstract class BaseCommand implements Command, SubCommandHoldable, Listener {

    private final SubCommandHolder subCommandHolder = new SubCommandHolder();
    protected final MessageProvider messageProvider;
    private final BoxScheduler scheduler;
    private Command commandOfNoArgument;

    protected BaseCommand(@NotNull MessageProvider messageProvider, @NotNull BoxScheduler scheduler) {
        this.messageProvider = messageProvider;
        this.scheduler = scheduler;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(args);

        var source = this.messageProvider.findSource(sender);

        if (!sender.hasPermission(getPermissionNode())) {
            ErrorMessages.NO_PERMISSION.apply(this.getPermissionNode()).source(source).send(sender);
            return;
        }

        if (args.length == 0) {
            if (commandOfNoArgument != null && sender.hasPermission(commandOfNoArgument.getPermissionNode())) {
                this.scheduler.runAsyncTask(() -> this.commandOfNoArgument.onCommand(sender, args));
            } else {
                ErrorMessages.NOT_ENOUGH_ARGUMENT.source(source).send(sender);
                sendHelp(sender, source);
            }
            return;
        }

        var optionalSubCommand = subCommandHolder.search(args[0]);

        if (optionalSubCommand.isEmpty()) {
            if (!args[0].equalsIgnoreCase("help")) {
                ErrorMessages.SUB_COMMAND_NOT_FOUND.source(source).send(sender);
            }
            sendHelp(sender, source);
            return;
        }

        var subCommand = optionalSubCommand.get();

        if (sender.hasPermission(subCommand.getPermissionNode())) {
            this.scheduler.runAsyncTask(() -> {
                try {
                    subCommand.onCommand(sender, args);
                } catch (Throwable e) {
                    CoreMessages.COMMAND_EXECUTION_ERROR_MSG.apply(e).source(source).send(sender);
                }
            });
        } else {
            ErrorMessages.NO_PERMISSION.apply(subCommand.getPermissionNode()).source(source).send(sender);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(args);

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

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return Component.text("/" + this.getName());
    }

    public void changeNoArgumentCommand(@Nullable Command command) {
        commandOfNoArgument = command;
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

    private void sendHelp(@NotNull CommandSender sender, @NotNull MiniMessageSource msgSrc) {
        CoreMessages.COMMAND_HELP_HEADER.apply("/" + this.getName()).source(msgSrc).send(sender);
        this.subCommandHolder.getSubCommands()
                .stream()
                .map(command -> command.getHelp(msgSrc))
                .forEach(sender::sendMessage);
    }
}
