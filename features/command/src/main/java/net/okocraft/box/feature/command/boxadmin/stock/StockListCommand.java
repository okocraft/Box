package net.okocraft.box.feature.command.boxadmin.stock;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import net.okocraft.box.feature.command.shared.SharedStockListCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class StockListCommand extends AbstractCommand {

    StockListCommand() {
        super("list", "box.admin.command.stock.list", Set.of("l"));
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.STOCK_LIST_HELP;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(getHelp());
            return;
        }

        var target = UserSearcher.search(args[2]);

        if (target != null) {
            sender.sendMessage(SharedStockListCommand.createStockList(
                    BoxAPI.api().getStockManager().getPersonalStockHolder(target),
                    3 < args.length ? Arrays.copyOfRange(args, 3, args.length) : null
            ));
        } else {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[2]));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            return Collections.emptyList();
        }

        if (args.length == 3) {
            return TabCompleter.players(args[2]);
        }

        if (args.length == 4) {
            return SharedStockListCommand.getArgumentTypes();
        }

        int index = args.length - 1;
        return SharedStockListCommand.createTabCompletion(args[index - 1], args[index]);
    }
}
