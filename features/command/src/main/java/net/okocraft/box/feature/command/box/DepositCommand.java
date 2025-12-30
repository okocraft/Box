package net.okocraft.box.feature.command.box;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.transaction.StockHolderTransaction;
import net.okocraft.box.api.transaction.TransactionResult;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.okocraft.box.api.message.Placeholders.AMOUNT;
import static net.okocraft.box.api.message.Placeholders.CURRENT;
import static net.okocraft.box.api.message.Placeholders.ITEM;

public class DepositCommand extends AbstractCommand {

    private static final String HELP = """
        <aqua>/box deposit [amount]<dark_gray> - <gray>Deposits the item in your main hand
        <aqua>/box deposit all<dark_gray> - <gray>Deposits all items in your inventory
        <aqua>/box deposit <item> [amount]<dark_gray> - <gray>Deposits specified item in your inventory""";

    private final MessageKey.Arg3<BoxItem, Integer, Integer> depositSuccess;
    private final MessageKey.Arg1<Integer> depositAllSuccess;
    private final MessageKey isAir;
    private final MessageKey itemNotRegistered;
    private final MessageKey notDeposited;
    private final MessageKey notFound;
    private final MessageKey help;

    public DepositCommand(@NotNull DefaultMessageCollector collector) {
        super("deposit", "box.command.deposit", Set.of("d"));

        this.depositSuccess = MessageKey.arg3(collector.add("box.command.box.deposit.success", "<gray>Deposited <aqua><item><gray>x<aqua><amount><gray> (Now <aqua><current><gray>)"), ITEM, AMOUNT, CURRENT);
        this.depositAllSuccess = MessageKey.arg1(collector.add("box.command.box.deposit.all-success", "<gray>Deposited <aqua><amount><gray> items in your inventory."), AMOUNT);
        this.isAir = MessageKey.key(collector.add("box.command.box.deposit.is-air", "<red>You have no item in your main hand."));
        this.itemNotRegistered = MessageKey.key(collector.add("box.command.box.deposit.item-not-registered", "<red>The item in your main hand is not registered."));
        this.notDeposited = MessageKey.key(collector.add("box.command.box.deposit.not-deposited", "<red>The specified item is not present in your inventory."));
        this.notFound = MessageKey.key(collector.add("box.command.box.deposit.not-found", "<red>No items could be deposited in your inventory."));
        this.help = MessageKey.key(collector.add("box.command.box.deposit.help", HELP));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ErrorMessages.COMMAND_ONLY_PLAYER);
            return;
        }

        if (args.length == 1) {
            this.depositItemInMainHand(player, Integer.MAX_VALUE);
            return;
        }

        ItemManager itemManager = BoxAPI.api().getItemManager();

        if (args.length == 2) {
            String arg = args[1];

            if (arg.equalsIgnoreCase("all")) {
                this.depositAll(player);
                return;
            }

            try {
                this.depositItemInMainHand(player, Math.max(Integer.parseInt(arg), 1));
                return;
            } catch (NumberFormatException ignored) {
            }

            itemManager.getBoxItem(arg).ifPresentOrElse(
                boxItem -> this.depositItem(player, boxItem, Integer.MAX_VALUE),
                () -> {
                    if (!arg.isEmpty() && arg.length() < 4 &&
                        (arg.charAt(0) == 'a' || arg.charAt(0) == 'A')) {
                        this.depositAll(player);
                    } else {
                        sender.sendMessage(ErrorMessages.ITEM_NOT_FOUND.apply(arg));
                    }
                }
            );

            return;
        }

        if (2 < args.length) {
            Optional<BoxItem> boxItem = itemManager.getBoxItem(args[1]);

            if (boxItem.isEmpty()) {
                sender.sendMessage(ErrorMessages.ITEM_NOT_FOUND.apply(args[1]));
                return;
            }

            int amount;

            try {
                amount = Math.max(Integer.parseInt(args[2]), 1);
            } catch (NumberFormatException e) {
                sender.sendMessage(ErrorMessages.INVALID_NUMBER.apply(args[2]));
                return;
            }

            this.depositItem(player, boxItem.get(), amount);
        }
    }

    private void depositItemInMainHand(@NotNull Player player, int limit) {
        BoxAPI.api().getScheduler().runEntityTask(player, () -> {
            if (limit < 1) {
                player.sendMessage(this.notDeposited);
                return;
            }

            ItemStack mainHand = player.getInventory().getItemInMainHand();

            if (mainHand.getType().isAir()) {
                player.sendMessage(this.isAir);
                return;
            }

            BoxItem boxItem = BoxAPI.api().getItemManager().getBoxItem(mainHand).orElse(null);

            if (boxItem == null) {
                player.sendMessage(this.itemNotRegistered);
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

            player.sendMessage(this.depositSuccess.apply(boxItem, amount, current));
        });
    }

    private void depositAll(@NotNull Player player) {
        BoxAPI.api().getScheduler().runEntityTask(player, () -> this.sendDepositResult(
            player, false,
            StockHolderTransaction.create(BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder())
                .depositAll()
                .fromInventory(player.getInventory(), CommandCauses.DEPOSIT)
        ));
    }

    private void depositItem(@NotNull Player player, @NotNull BoxItem boxItem, int amount) {
        BoxAPI.api().getScheduler().runEntityTask(player, () -> this.sendDepositResult(
            player, true,
            StockHolderTransaction.create(BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder())
                .deposit(boxItem, amount)
                .fromInventory(player.getInventory(), CommandCauses.DEPOSIT)
        ));
    }

    private void sendDepositResult(@NotNull Player player, boolean isItemSpecified, @NotNull List<TransactionResult> resultList) {
        int deposited = resultList.stream().mapToInt(TransactionResult::amount).sum();
        if (0 < deposited) {
            player.sendMessage(this.depositAllSuccess.apply(deposited));
        } else if (isItemSpecified) {
            player.sendMessage(this.notFound);
        } else {
            player.sendMessage(this.notDeposited);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            String itemNameFilter = args[1].toLowerCase(Locale.ENGLISH);

            //noinspection ConstantConditions
            List<@NotNull String> result =
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
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }
}
