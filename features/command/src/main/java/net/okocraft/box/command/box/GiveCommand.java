package net.okocraft.box.command.box;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.command.message.BoxMessage;
import net.okocraft.box.command.util.TabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class GiveCommand extends AbstractCommand {

    public GiveCommand() {
        super("give", "box.command.give", Set.of("g"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            return;
        }

        var target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[1]));
            return;
        }

        if (!target.hasPermission(getPermissionNode())) {
            sender.sendMessage(BoxMessage.GIVE_TARGET_NO_PERMISSION.apply(target, getPermissionNode()));
            return;
        }

        var optionalBoxItem = BoxProvider.get().getItemManager().getBoxItem(args[2]);

        if (optionalBoxItem.isEmpty()) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[2]));
            return;
        }

        var boxItem = optionalBoxItem.get();

        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();
        var currentStock = stockHolder.getAmount(boxItem);

        if (currentStock < 1) {
            player.sendMessage(BoxMessage.GIVE_NO_STOCK.apply(boxItem));
            return;
        }

        int amount;

        if (3 < args.length) {
            try {
                amount = Math.min(currentStock, Math.max(Integer.parseInt(args[3]), 1));
            } catch (NumberFormatException e) {
                player.sendMessage(GeneralMessage.ERROR_COMMAND_INVALID_NUMBER.apply(args[3]));
                return;
            }
        } else {
            amount = 1;
        }

        var currentSender = stockHolder.decrease(boxItem, amount);
        var currentTarget =
                BoxProvider.get().getBoxPlayerMap().get(target)
                        .getCurrentStockHolder().increase(boxItem, amount);

        player.sendMessage(BoxMessage.GIVE_SUCCESS_SENDER.apply(target.getName(), boxItem, amount, currentSender));
        target.sendMessage(BoxMessage.GIVE_SUCCESS_TARGET.apply(player.getName(), boxItem, amount, currentTarget));
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            return TabCompleter.players(args[1], getPermissionNode());
        }

        if (args.length == 3) {
            var itemNameFilter = args[2].toUpperCase(Locale.ROOT);
            var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();

            return stockHolder.getStockedItems().stream()
                    .map(BoxItem::getPlainName)
                    .filter(itemName -> itemName.startsWith(itemNameFilter))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
