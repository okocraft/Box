package net.okocraft.box.feature.command.boxadmin.stock;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.command.SubCommandHoldable;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StockCommand extends AbstractCommand implements SubCommandHoldable {

    private final SubCommandHolder subCommandHolder;

    public StockCommand() {
        super("stock", "box.admin.command.stock", Set.of("s", "st"));
        this.subCommandHolder = new SubCommandHolder(
                StockModifyCommands.give(),
                new StockInfoCommand(),
                new StockListCommand(),
                new StockResetCommand(),
                StockModifyCommands.set(),
                StockModifyCommands.take()
        );
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.STOCK_HELP
                .append(Component.newline())
                .append(
                        Component.join(
                                JoinConfiguration.separator(Component.newline()),
                                subCommandHolder.getSubCommands().stream().map(Command::getHelp).toList()
                        )
                );
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(getHelp());
            return;
        }

        var optionalSubCommand = subCommandHolder.search(args[1]);

        if (optionalSubCommand.isEmpty()) {
            if (!args[1].equalsIgnoreCase("help")) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_SUBCOMMAND_NOT_FOUND);
            }
            sender.sendMessage(getHelp());
            return;
        }

        var subCommand = optionalSubCommand.get();

        if (sender.hasPermission(subCommand.getPermissionNode())) {
            subCommand.onCommand(sender, args);
        } else {
            sender.sendMessage(GeneralMessage.ERROR_NO_PERMISSION.apply(subCommand.getPermissionNode()));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return subCommandHolder.getSubCommands().stream()
                    .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                    .map(Command::getName)
                    .filter(cmdName -> cmdName.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        } else {
            return subCommandHolder.search(args[1])
                    .filter(cmd -> sender.hasPermission(cmd.getPermissionNode()))
                    .map(cmd -> cmd.onTabComplete(sender, args))
                    .orElse(Collections.emptyList());
        }
    }

    @Override
    public @NotNull SubCommandHolder getSubCommandHolder() {
        return subCommandHolder;
    }
}
