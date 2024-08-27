package net.okocraft.box.feature.command.box;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.arg.Arg3;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.transaction.StockHolderTransaction;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.arg.Arg3.arg3;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
import static net.okocraft.box.api.message.Placeholders.AMOUNT;
import static net.okocraft.box.api.message.Placeholders.CURRENT;
import static net.okocraft.box.api.message.Placeholders.ITEM;

public class WithdrawCommand extends AbstractCommand {

    private final Arg3<BoxItem, Integer, Integer> success;
    private final MiniMessageBase stopped;
    private final Arg1<BoxItem> noStock;
    private final MiniMessageBase inventoryFull;
    private final MiniMessageBase help;

    public WithdrawCommand(@NotNull DefaultMessageCollector collector) {
        super("withdraw", "box.command.withdraw", Set.of("w"));
        this.success = arg3(collector.add("box.command.box.withdraw.success", "<gray>Withdrew <aqua><item><gray>x<aqua><amount><gray> (Now <aqua><current><gray>)."), ITEM, AMOUNT, CURRENT);
        this.stopped = messageKey(collector.add("box.command.box.withdraw.stopped", "<red>Withdrawal was cancelled because your inventory has no more space."));
        this.noStock = arg1(collector.add("box.command.box.withdraw.no-stock", "<red>Item <aqua><item><red> is out of stock."), ITEM);
        this.inventoryFull = messageKey(collector.add("box.command.box.withdraw.inventory-full", "<red>Your inventory is full."));
        this.help = messageKey(collector.add("box.command.box.withdraw.help", "<aqua>/box withdraw <item> [amount]<dark_gray> - <gray>Withdraws item"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (!(sender instanceof Player player)) {
            ErrorMessages.COMMAND_ONLY_PLAYER.source(msgSrc).send(sender);
            return;
        }

        if (args.length < 2) {
            ErrorMessages.NOT_ENOUGH_ARGUMENT.source(msgSrc).send(sender);
            sender.sendMessage(this.getHelp(msgSrc));
            return;
        }

        var optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(args[1]);

        if (optionalBoxItem.isEmpty()) {
            ErrorMessages.ITEM_NOT_FOUND.apply(args[1]).source(msgSrc).send(sender);
            return;
        }

        var boxItem = optionalBoxItem.get();

        var stockHolder = BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder();

        var currentStock = stockHolder.getAmount(boxItem);

        if (currentStock < 1) {
            this.noStock.apply(boxItem).source(msgSrc).send(sender);
            return;
        }

        int amount;

        if (2 < args.length) {
            try {
                amount = Math.min(currentStock, Math.max(Integer.parseInt(args[2]), 1));
            } catch (NumberFormatException e) {
                ErrorMessages.INVALID_NUMBER.apply(args[2]).source(msgSrc).send(sender);
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
                this.inventoryFull.source(msgSrc).send(sender);
                return;
            }

            if (withdrawnAmount != amount) {
                this.stopped.source(msgSrc).send(sender);
            }

            this.success.apply(boxItem, withdrawnAmount, stockHolder.getAmount(boxItem)).source(msgSrc).send(sender);
        });
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length != 2 || !(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        var itemNameFilter = args[1].toLowerCase(Locale.ENGLISH);
        var stockHolder = BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder();

        return stockHolder.getStockedItems().stream()
            .map(BoxItem::getPlainName)
            .filter(itemName -> itemName.toLowerCase(Locale.ENGLISH).startsWith(itemNameFilter))
            .toList();
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }
}
