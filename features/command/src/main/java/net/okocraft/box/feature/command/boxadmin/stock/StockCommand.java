package net.okocraft.box.feature.command.boxadmin.stock;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.command.SubCommandHoldable;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.feature.command.shared.SharedStockListCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class StockCommand extends AbstractCommand implements SubCommandHoldable {

    private final SubCommandHolder subCommandHolder;

    public StockCommand(@NotNull DefaultMessageCollector collector, @NotNull SharedStockListCommand sharedStockListCommand) {
        super("stock", "box.admin.command.stock", Set.of("s", "st"));
        this.subCommandHolder = new SubCommandHolder(
            new StockModifyCommands.StockGiveCommand(collector),
            new StockModifyCommands.StockTakeCommand(collector),
            new StockModifyCommands.StockSetCommand(collector),
            new StockInfoCommand(collector),
            new StockListCommand(collector, sharedStockListCommand),
            new StockResetCommand(collector)
        );
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ErrorMessages.NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(this.getHelp());
            return;
        }

        Optional<Command> optionalSubCommand = this.subCommandHolder.search(args[1]);

        if (optionalSubCommand.isEmpty()) {
            if (!args[1].equalsIgnoreCase("help")) {
                sender.sendMessage(ErrorMessages.SUB_COMMAND_NOT_FOUND);
            }
            sender.sendMessage(this.getHelp());
            return;
        }

        Command subCommand = optionalSubCommand.get();

        if (sender.hasPermission(subCommand.getPermissionNode())) {
            subCommand.onCommand(sender, args);
        } else {
            sender.sendMessage(ErrorMessages.NO_PERMISSION.apply(subCommand.getPermissionNode()));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return this.subCommandHolder.getSubCommands().stream()
                .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                .map(Command::getName)
                .filter(cmdName -> cmdName.startsWith(args[1].toLowerCase(Locale.ROOT)))
                .toList();
        } else {
            return this.subCommandHolder.search(args[1])
                .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                .map(cmd -> cmd.onTabComplete(sender, args))
                .orElse(Collections.emptyList());
        }
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return Component.join(
            JoinConfiguration.newlines(),
            this.subCommandHolder.getSubCommands()
                .stream()
                .map(Command::getHelp)
                .toList()
        );
    }

    @Override
    public @NotNull SubCommandHolder getSubCommandHolder() {
        return this.subCommandHolder;
    }
}
