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
import net.okocraft.box.api.transaction.TransactionResult;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.arg.Arg3.arg3;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
import static net.okocraft.box.api.message.Placeholders.AMOUNT;
import static net.okocraft.box.api.message.Placeholders.CURRENT;
import static net.okocraft.box.api.message.Placeholders.ITEM;

public class DepositCommand extends AbstractCommand {

    private static final String HELP = """
            <aqua>/box deposit [amount]<dark_gray> - <gray>Deposits the item in your main hand
            <aqua>/box deposit all<dark_gray> - <gray>Deposits all items in your inventory
            <aqua>/box deposit <item> [amount]<dark_gray> - <gray>Deposits specified item in your inventory""";

    private final Arg3<BoxItem, Integer, Integer> depositSuccess;
    private final Arg1<Integer> depositAllSuccess;
    private final MiniMessageBase isAir;
    private final MiniMessageBase itemNotRegistered;
    private final MiniMessageBase notDeposited;
    private final MiniMessageBase notFound;
    private final MiniMessageBase help;

    public DepositCommand(@NotNull DefaultMessageCollector collector) {
        super("deposit", "box.command.deposit", Set.of("d"));

        this.depositSuccess = arg3(collector.add("box.command.box.deposit.success", "<gray>Deposited <aqua><item><gray>x<aqua><amount><gray> (Now <aqua><current><gray>)"), ITEM, AMOUNT, CURRENT);
        this.depositAllSuccess = arg1(collector.add("box.command.box.deposit.all-success", "<gray>Deposited <aqua><amount><gray> items in your inventory."), AMOUNT);
        this.isAir = messageKey(collector.add("box.command.box.deposit.is-air", "<red>You have no item in your main hand."));
        this.itemNotRegistered = messageKey(collector.add("box.command.box.deposit.item-not-registered", "<red>The item in your main hand is not registered."));
        this.notDeposited = messageKey(collector.add("box.command.box.deposit.not-deposited", "<red>The specified item is not present in your inventory."));
        this.notFound = messageKey(collector.add("box.command.box.deposit.not-found", "<red>No items could be deposited in your inventory."));
        this.help = messageKey(collector.add("box.command.box.deposit.help", HELP));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (!(sender instanceof Player player)) {
            ErrorMessages.COMMAND_ONLY_PLAYER.source(msgSrc).send(sender);
            return;
        }

        if (args.length == 1) {
            depositItemInMainHand(player, Integer.MAX_VALUE, msgSrc);
            return;
        }

        var itemManager = BoxAPI.api().getItemManager();

        if (args.length == 2) {
            var arg = args[1];

            if (arg.equalsIgnoreCase("all")) {
                depositAll(player, msgSrc);
                return;
            }

            try {
                depositItemInMainHand(player, Math.max(Integer.parseInt(arg), 1), msgSrc);
                return;
            } catch (NumberFormatException ignored) {
            }

            itemManager.getBoxItem(arg).ifPresentOrElse(
                    boxItem -> depositItem(player, msgSrc, boxItem, Integer.MAX_VALUE),
                    () -> {
                        if (!arg.isEmpty() && arg.length() < 4 &&
                                (arg.charAt(0) == 'a' || arg.charAt(0) == 'A')) {
                            depositAll(player, msgSrc);
                        } else {
                            ErrorMessages.ITEM_NOT_FOUND.apply(arg).source(msgSrc).send(sender);
                        }
                    }
            );

            return;
        }

        if (2 < args.length) {
            var boxItem = itemManager.getBoxItem(args[1]);

            if (boxItem.isEmpty()) {
                ErrorMessages.ITEM_NOT_FOUND.apply(args[1]).source(msgSrc).send(sender);
                return;
            }

            int amount;

            try {
                amount = Math.max(Integer.parseInt(args[2]), 1);
            } catch (NumberFormatException e) {
                ErrorMessages.INVALID_NUMBER.apply(args[2]).source(msgSrc).send(sender);
                return;
            }

            depositItem(player, msgSrc, boxItem.get(), amount);
        }
    }

    private void depositItemInMainHand(@NotNull Player player, int limit, @NotNull MiniMessageSource msgSrc) {
        BoxAPI.api().getScheduler().runEntityTask(player, () -> {
            if (limit < 1) {
                this.notDeposited.source(msgSrc).send(player);
                return;
            }

            var mainHand = player.getInventory().getItemInMainHand();

            if (mainHand.getType().isAir()) {
                this.isAir.source(msgSrc).send(player);
                return;
            }

            var boxItem = BoxAPI.api().getItemManager().getBoxItem(mainHand).orElse(null);

            if (boxItem == null) {
                this.itemNotRegistered.source(msgSrc).send(player);
                return;
            }

            int amount = Math.min(limit, mainHand.getAmount());
            int remaining = mainHand.getAmount() - amount;

            int current = BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder().increase(boxItem, amount, CommandCauses.DEPOSIT);

            if (0 < remaining) {
                player.getInventory().setItemInMainHand(mainHand.asQuantity(remaining));
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            this.depositSuccess.apply(boxItem, amount, current).source(msgSrc).send(player);
        });
    }

    private void depositAll(@NotNull Player player, @NotNull MiniMessageSource msgSrc) {
        BoxAPI.api().getScheduler().runEntityTask(player, () -> this.sendDepositResult(
                player, msgSrc, false,
                StockHolderTransaction.create(BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder())
                        .depositAll()
                        .fromInventory(player.getInventory(), CommandCauses.DEPOSIT)
        ));
    }

    private void depositItem(@NotNull Player player, @NotNull MiniMessageSource msgSrc, @NotNull BoxItem boxItem, int amount) {
        BoxAPI.api().getScheduler().runEntityTask(player, () -> this.sendDepositResult(
                player, msgSrc, true,
                StockHolderTransaction.create(BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder())
                        .deposit(boxItem, amount)
                        .fromInventory(player.getInventory(), CommandCauses.DEPOSIT)
        ));
    }

    private void sendDepositResult(@NotNull Player player, @NotNull MiniMessageSource msgSrc, boolean isItemSpecified, @NotNull List<TransactionResult> resultList) {
        int deposited = resultList.stream().mapToInt(TransactionResult::amount).sum();
        if (0 < deposited) {
            this.depositAllSuccess.apply(deposited).source(msgSrc).send(player);
        } else if (isItemSpecified) {
            this.notFound.source(msgSrc).send(player);
        } else {
            this.notDeposited.source(msgSrc).send(player);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            var itemNameFilter = args[1].toLowerCase(Locale.ENGLISH);

            //noinspection ConstantConditions
            var result =
                    Arrays.stream(player.getInventory().getStorageContents())
                            .filter(Objects::nonNull)
                            .map(BoxAPI.api().getItemManager()::getBoxItem)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(BoxItem::getPlainName)
                            .filter(itemName -> itemName.toLowerCase(Locale.ENGLISH).startsWith(itemNameFilter))
                            .collect(Collectors.toList());

            if ("all".startsWith(args[1].toLowerCase(Locale.ENGLISH))) {
                result.add("all");
            }

            return result;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }
}
