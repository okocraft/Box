package net.okocraft.box.feature.command.box;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import net.okocraft.box.feature.command.message.BoxMessage;
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
            sender.sendMessage(getHelp());
            return;
        }

        var target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[1]));
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(BoxMessage.GIVE_SELF);
            return;
        }

        if (!target.hasPermission(getPermissionNode())) {
            sender.sendMessage(BoxMessage.GIVE_TARGET_NO_PERMISSION.apply(target, getPermissionNode()));
            return;
        }

        if (BoxProvider.get().isDisabledWorld(target)) {
            sender.sendMessage(BoxMessage.GIVE_TARGET_IS_IN_DISABLED_WORLD.apply(target, target.getWorld()));
            return;
        }

        var optionalBoxItem = BoxProvider.get().getItemManager().getBoxItem(args[2]);

        if (optionalBoxItem.isEmpty()) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[2]));
            return;
        }

        var boxItem = optionalBoxItem.get();

        var playerMap = BoxProvider.get().getBoxPlayerMap();

        var senderBoxPlayer = playerMap.get(player);
        var stockHolder = senderBoxPlayer.getCurrentStockHolder();
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

        if (!playerMap.isLoaded(target)) {
            if (playerMap.isScheduledLoading(player)) {
                sender.sendMessage(GeneralMessage.ERROR_TARGET_PLAYER_LOADING.apply(target));
            } else {
                sender.sendMessage(GeneralMessage.ERROR_TARGET_PLAYER_NOT_LOADED.apply(target));
            }
            return;
        }

        var targetBoxPlayer = playerMap.get(target);

        var senderCurrent = stockHolder.decrease(boxItem, amount, new CommandCauses.Give(targetBoxPlayer));
        var targetCurrent = targetBoxPlayer.getCurrentStockHolder().increase(boxItem, amount, new CommandCauses.Receive(senderBoxPlayer));

        player.sendMessage(BoxMessage.GIVE_SUCCESS_SENDER.apply(target.getName(), boxItem, amount, senderCurrent));
        target.sendMessage(BoxMessage.GIVE_SUCCESS_TARGET.apply(player.getName(), boxItem, amount, targetCurrent));
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

    @Override
    public @NotNull Component getHelp() {
        return BoxMessage.GIVE_HELP;
    }
}
