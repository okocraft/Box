package net.okocraft.box.feature.command.box;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.transaction.StockHolderTransaction;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static net.okocraft.box.api.message.Placeholders.AMOUNT;
import static net.okocraft.box.api.message.Placeholders.CURRENT;
import static net.okocraft.box.api.message.Placeholders.ITEM;

public class WithdrawCommand extends AbstractCommand {

    private final MessageKey.Arg3<BoxItem, Integer, Integer> success;
    private final MessageKey stopped;
    private final MessageKey.Arg1<BoxItem> noStock;
    private final MessageKey inventoryFull;
    private final MessageKey help;

    public WithdrawCommand(@NotNull DefaultMessageCollector collector) {
        super("withdraw", "box.command.withdraw", Set.of("w"));
        this.success = MessageKey.arg3(collector.add("box.command.box.withdraw.success", "<gray>Withdrew <aqua><item><gray>x<aqua><amount><gray> (Now <aqua><current><gray>)."), ITEM, AMOUNT, CURRENT);
        this.stopped = MessageKey.key(collector.add("box.command.box.withdraw.stopped", "<red>Withdrawal was cancelled because your inventory has no more space."));
        this.noStock = MessageKey.arg1(collector.add("box.command.box.withdraw.no-stock", "<red>Item <aqua><item><red> is out of stock."), ITEM);
        this.inventoryFull = MessageKey.key(collector.add("box.command.box.withdraw.inventory-full", "<red>Your inventory is full."));
        this.help = MessageKey.key(collector.add("box.command.box.withdraw.help", "<aqua>/box withdraw <item> [amount]<dark_gray> - <gray>Withdraws item"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ErrorMessages.COMMAND_ONLY_PLAYER);
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ErrorMessages.NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(this.getHelp());
            return;
        }

        Optional<BoxItem> optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(args[1]);

        if (optionalBoxItem.isEmpty()) {
            sender.sendMessage(ErrorMessages.ITEM_NOT_FOUND.apply(args[1]));
            return;
        }

        BoxItem boxItem = optionalBoxItem.get();

        StockHolder stockHolder = BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder();

        int currentStock = stockHolder.getAmount(boxItem);

        if (currentStock < 1) {
            sender.sendMessage(this.noStock.apply(boxItem));
            return;
        }

        int amount;

        if (2 < args.length) {
            try {
                amount = Math.min(currentStock, Math.max(Integer.parseInt(args[2]), 1));
            } catch (NumberFormatException e) {
                ErrorMessages.INVALID_NUMBER.apply(args[2]);
                return;
            }
        } else {
            amount = 1;
        }

        BoxAPI.api().getScheduler().runEntityTask(player, () -> {
            int withdrawnAmount =
                StockHolderTransaction.create(stockHolder)
                    .withdraw(boxItem, amount)
                    .toInventory(player.getInventory(), CommandCauses.WITHDRAW).amount();

            if (withdrawnAmount == 0) {
                sender.sendMessage(this.inventoryFull);
                return;
            }

            if (withdrawnAmount != amount) {
                sender.sendMessage(this.stopped);
            }

            sender.sendMessage(this.success.apply(boxItem, withdrawnAmount, stockHolder.getAmount(boxItem)));
        });
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 2 || !(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        String itemNameFilter = args[1].toLowerCase(Locale.ENGLISH);
        StockHolder stockHolder = BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder();

        return stockHolder.getStockedItems().stream()
            .map(BoxItem::getPlainName)
            .filter(itemName -> itemName.toLowerCase(Locale.ENGLISH).startsWith(itemNameFilter))
            .toList();
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }
}
