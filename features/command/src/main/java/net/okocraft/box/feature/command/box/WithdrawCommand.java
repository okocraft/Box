package net.okocraft.box.feature.command.box;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.api.transaction.TransactionResultType;
import net.okocraft.box.feature.command.message.BoxMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
            sender.sendMessage(getHelp());
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

        var result =
                BoxProvider.get().getTaskFactory()
                        .supply(() -> InventoryTransaction.withdraw(player.getInventory(), boxItem, amount))
                        .join();

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
        if (args.length != 2 || !(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        var itemNameFilter = args[1].toUpperCase(Locale.ROOT);
        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();

        return stockHolder.getStockedItems().stream()
                .map(BoxItem::getPlainName)
                .filter(itemName -> itemName.startsWith(itemNameFilter))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxMessage.WITHDRAW_HELP;
    }
}
