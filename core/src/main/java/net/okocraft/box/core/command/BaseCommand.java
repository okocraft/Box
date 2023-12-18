package net.okocraft.box.core.command;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.command.SubCommandHoldable;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.core.message.ErrorMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public abstract class BaseCommand implements Command, SubCommandHoldable, Listener {

    private final SubCommandHolder subCommandHolder = new SubCommandHolder();
    private Command commandOfNoArgument;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(args);

        if (!sender.hasPermission(getPermissionNode())) {
            sender.sendMessage(GeneralMessage.ERROR_NO_PERMISSION.apply(getPermissionNode()));
            return;
        }

        if (sender instanceof Player player && !BoxProvider.get().canUseBox(player)) {
            sender.sendMessage(GeneralMessage.ERROR_DISABLED_WORLD.apply(player.getWorld()));
            return;
        }

        if (args.length == 0) {
            if (commandOfNoArgument != null && sender.hasPermission(commandOfNoArgument.getPermissionNode())) {
                runCommandAsync(commandOfNoArgument, sender, args);
            } else {
                sender.sendMessage(ErrorMessages.ERROR_COMMAND_NO_ARGUMENT);
                sendHelp(sender);
            }
            return;
        }

        var optionalSubCommand = subCommandHolder.search(args[0]);

        if (optionalSubCommand.isEmpty()) {
            if (!args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_SUBCOMMAND_NOT_FOUND);
            }
            sendHelp(sender);
            return;
        }

        var subCommand = optionalSubCommand.get();

        if (sender.hasPermission(subCommand.getPermissionNode())) {
            runCommandAsync(subCommand, sender, args);
        } else {
            sender.sendMessage(GeneralMessage.ERROR_NO_PERMISSION.apply(subCommand.getPermissionNode()));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(args);

        if (args.length == 0 || !sender.hasPermission(getPermissionNode())) {
            return Collections.emptyList();
        }

        if (sender instanceof Player player && !BoxProvider.get().canUseBox(player)) {
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

    private void sendHelp(@NotNull CommandSender sender) {
        sender.sendMessage(
                text("============================== ", DARK_GRAY)
                        .append(text("Box Help", GOLD))
                        .append(text(" ============================== ", DARK_GRAY))
        );

        sender.sendMessage(getHelp());
        subCommandHolder.getSubCommands()
                .stream()
                .map(Command::getHelp)
                .forEach(sender::sendMessage);
    }

    private void runCommandAsync(@NotNull Command command, @NotNull CommandSender sender, @NotNull String[] args) {
        BoxProvider.get().getScheduler().runAsyncTask(() -> command.onCommand(sender, args));
    }
}
