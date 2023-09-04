package net.okocraft.box.feature.command.box;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.transaction.StockHolderTransaction;
import net.okocraft.box.api.transaction.TransactionResult;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import net.okocraft.box.feature.command.message.BoxMessage;
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
            var arg = args[1];

            if (arg.equalsIgnoreCase("all")) {
                depositAll(player);
                return;
            }

            try {
                depositItemInMainHand(player, Math.max(Integer.parseInt(arg), 1));
                return;
            } catch (NumberFormatException ignored) {
            }

            itemManager.getBoxItem(arg).ifPresentOrElse(
                    boxItem -> depositItem(player, boxItem, Integer.MAX_VALUE),
                    () -> {
                        if (!arg.isEmpty() && arg.length() < 4 &&
                                (arg.charAt(0) == 'a' || arg.charAt(0) == 'A')) {
                            depositAll(player);
                        } else {
                            player.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(arg));
                        }
                    }
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

    private void depositItemInMainHand(@NotNull Player player, int limit) {
        BoxProvider.get().getScheduler().runEntityTask(player, () -> {
            if (limit < 1) {
                player.sendMessage(BoxMessage.DEPOSIT_NOT_DEPOSITED);
                return;
            }

            var mainHand = player.getInventory().getItemInMainHand();

            if (mainHand.getType().isAir()) {
                player.sendMessage(BoxMessage.DEPOSIT_IS_AIR);
                return;
            }

            var boxItem = BoxProvider.get().getItemManager().getBoxItem(mainHand).orElse(null);

            if (boxItem == null) {
                player.sendMessage(BoxMessage.DEPOSIT_ITEM_NOT_REGISTERED);
                return;
            }

            int amount = Math.min(limit, mainHand.getAmount());
            int remaining = mainHand.getAmount() - amount;

            int current = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder().increase(boxItem, amount, CommandCauses.DEPOSIT);

            if (0 < remaining) {
                player.getInventory().setItemInMainHand(mainHand.asQuantity(remaining));
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            player.sendMessage(BoxMessage.DEPOSIT_SUCCESS.apply(boxItem, amount, current));
        });
    }

    private void depositAll(@NotNull Player player) {
        BoxProvider.get().getScheduler().runEntityTask(player, () -> {
            var resultList =
                    StockHolderTransaction.create(BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder())
                            .depositAll()
                            .fromInventory(player.getInventory(), CommandCauses.DEPOSIT);

            int deposited = calculateDepositedAmount(resultList);
            player.sendMessage(0 < deposited ? BoxMessage.DEPOSIT_ALL_SUCCESS.apply(deposited) : BoxMessage.DEPOSIT_NOT_DEPOSITED);
        });
    }

    private void depositItem(@NotNull Player player, @NotNull BoxItem boxItem, int amount) {
        BoxProvider.get().getScheduler().runEntityTask(player, () -> {
            var resultList =
                    StockHolderTransaction.create(BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder())
                            .deposit(boxItem, amount)
                            .fromInventory(player.getInventory(), CommandCauses.DEPOSIT);

            int deposited = calculateDepositedAmount(resultList);
            player.sendMessage(0 < deposited ? BoxMessage.DEPOSIT_ALL_SUCCESS.apply(deposited) : BoxMessage.DEPOSIT_NOT_FOUND);
        });
    }

    private int calculateDepositedAmount(@NotNull List<TransactionResult> resultList) {
        return resultList.isEmpty() ? 0 : resultList.stream().mapToInt(TransactionResult::amount).sum();
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
                            .map(BoxProvider.get().getItemManager()::getBoxItem)
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
    public @NotNull Component getHelp() {
        return Component.text()
                .append(BoxMessage.DEPOSIT_HELP_1).append(Component.newline())
                .append(BoxMessage.DEPOSIT_HELP_2).append(Component.newline())
                .append(BoxMessage.DEPOSIT_HELP_3)
                .build();
    }
}
