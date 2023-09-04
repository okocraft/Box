package net.okocraft.box.feature.command.boxadmin.stock;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class StockInfoCommand extends AbstractCommand {

    StockInfoCommand() {
        super("info", "box.admin.command.stock.info", Set.of("i"));
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.STOCK_INFO_HELP;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(getHelp());
            return;
        }

        var boxItem = BoxProvider.get().getItemManager().getBoxItem(args[3]);

        if (boxItem.isEmpty()) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[3]));
            return;
        }

        var target = UserSearcher.search(args[2]);

        if (target != null) {
            var stockHolder = BoxProvider.get().getStockManager().getPersonalStockHolder(target);
            var message = BoxAdminMessage.STOCK_INFO_AMOUNT.apply(target, boxItem.get(), stockHolder.getAmount(boxItem.get()));
            sender.sendMessage(message);
        } else {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[2]));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 3) {
            return TabCompleter.players(args[2]);
        }

        if (args.length == 4) {
            return TabCompleter.itemNames(args[3]);
        }

        return Collections.emptyList();
    }
}
