package net.okocraft.box.command.box;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.api.transaction.TransactionResultList;
import net.okocraft.box.api.transaction.TransactionResultType;
import net.okocraft.box.command.message.BoxMessage;
import net.okocraft.box.command.util.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DepositCommand extends AbstractCommand {

    public DepositCommand() {
        super("deposit", "box.command.deposit", Set.of("d"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        if (args.length == 1) {
            depositItemInMainHand(player, Integer.MAX_VALUE);
            return;
        }

        var itemManager = BoxProvider.get().getItemManager();

        if (args.length == 2) {
            if (args[1].equalsIgnoreCase("all")) {
                depositAll(player);
                return;
            }

            try {
                depositItemInMainHand(player, Math.max(Integer.parseInt(args[1]), 1));
                return;
            } catch (NumberFormatException ignored) {
            }

            itemManager.getBoxItem(args[1]).ifPresentOrElse(
                    boxItem -> depositItem(player, boxItem, Integer.MAX_VALUE),
                    () -> player.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[1]))
            );

            return;
        }

        if (2 < args.length) {
            var boxItem = itemManager.getBoxItem(args[1]);

            if (boxItem.isEmpty()) {
                player.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[1]));
                return;
            }

            int amount;

            try {
                amount = Math.max(Integer.parseInt(args[2]), 1);
            } catch (NumberFormatException e) {
                player.sendMessage(GeneralMessage.ERROR_COMMAND_INVALID_NUMBER.apply(args[2]));
                return;
            }

            depositItem(player, boxItem.get(), amount);
        }
    }

    private void depositItemInMainHand(@NotNull Player player, int amount) {
        var result = InventoryTransaction.depositItemInMainHand(player, amount);

        if (result.getType().isModified()) {
            var item = result.getItem();
            var deposited = result.getAmount();

            var current = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder().increase(item, deposited);

            player.sendMessage(BoxMessage.DEPOSIT_SUCCESS.apply(item, deposited, current));
        } else {
            player.sendMessage(
                    switch (result.getType()) {
                        case IS_AIR -> BoxMessage.DEPOSIT_IS_AIR;
                        case ITEM_NOT_REGISTERED -> BoxMessage.DEPOSIT_ITEM_NOT_REGISTERED;
                        default -> BoxMessage.DEPOSIT_NOT_DEPOSITED;
                    }
            );
        }
    }

    private void depositAll(@NotNull Player player) {
        var resultList = InventoryTransaction.depositItemsInInventory(player.getInventory());

        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();

        var deposited = processTransactionResultList(player, resultList, stockHolder);

        if (0 < deposited) {
            player.sendMessage(BoxMessage.DEPOSIT_ALL_SUCCESS.apply(deposited));
        }
    }

    private void depositItem(@NotNull Player player, @NotNull BoxItem boxItem, int amount) {
        var resultList = InventoryTransaction.depositItem(player.getInventory(), boxItem, amount);

        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();

        var deposited = processTransactionResultList(player, resultList, stockHolder);

        if (0 < deposited) {
            player.sendMessage(BoxMessage.DEPOSIT_SUCCESS.apply(boxItem, deposited, stockHolder.getAmount(boxItem)));
        }
    }

    private int processTransactionResultList(@NotNull Player player, @NotNull TransactionResultList resultList,
                                             @NotNull StockHolder stockHolder) {
        var resultListType = resultList.getType();

        if (!resultListType.isModified()) {
            player.sendMessage(
                    resultListType == TransactionResultType.NOT_FOUND ?
                            BoxMessage.DEPOSIT_NOT_FOUND :
                            BoxMessage.DEPOSIT_NOT_DEPOSITED
            );

            return 0;
        }

        var counter = new AtomicInteger();

        resultList.getResultList()
                .stream()
                .filter(result -> result.getType().isModified())
                .forEach(result -> {
                    stockHolder.increase(result.getItem(), result.getAmount());
                    counter.addAndGet(result.getAmount());
                });

        return counter.get();
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            var result = TabCompleter.itemNames(args[1]);

            if ("all".startsWith(args[1].toLowerCase(Locale.ROOT))) {
                result.add("all");
            }

            return result;
        } else {
            return Collections.emptyList();
        }
    }
}