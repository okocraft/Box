package net.okocraft.box.command.box;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.api.transaction.TransactionResultType;
import net.okocraft.box.command.message.BoxMessage;
import net.okocraft.box.command.util.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class WithdrawCommand extends AbstractCommand {

    public WithdrawCommand() {
        super("withdraw", "box.command.withdraw", Set.of("w"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            return;
        }

        var optionalBoxItem = BoxProvider.get().getItemManager().getBoxItem(args[1]);

        if (optionalBoxItem.isEmpty()) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[1]));
            return;
        }

        var boxItem = optionalBoxItem.get();

        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();

        var currentStock = stockHolder.getAmount(boxItem);

        if (currentStock < 1) {
            sender.sendMessage(BoxMessage.WITHDRAW_NO_STOCK.apply(boxItem));
            return;
        }

        int amount;

        if (2 < args.length) {
            try {
                amount = Math.min(currentStock, Math.max(Integer.parseInt(args[2]), 1));
            } catch (NumberFormatException e) {
                player.sendMessage(GeneralMessage.ERROR_COMMAND_INVALID_NUMBER.apply(args[2]));
                return;
            }
        } else {
            amount = 1;
        }

        var result = InventoryTransaction.withdraw(player.getInventory(), boxItem, amount);
        var resultType = result.getType();

        if (resultType.isModified()) {
            var current = stockHolder.decrease(result.getItem(), result.getAmount());

            if (resultType == TransactionResultType.WITHDREW) {
                player.sendMessage(BoxMessage.WITHDRAW_SUCCESS.apply(boxItem, result.getAmount(), current));
            } else {
                player.sendMessage(BoxMessage.WITHDRAW_PARTIAL_SUCCESS.apply(boxItem, result.getAmount(), current));
            }
        } else {
            player.sendMessage(BoxMessage.WITHDRAW_INVENTORY_IS_FULL);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return TabCompleter.itemNames(args[1]);
        } else {
            return Collections.emptyList();
        }
    }
}
