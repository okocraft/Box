package net.okocraft.box.feature.command.box;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.okocraft.box.api.message.Placeholders.AMOUNT;
import static net.okocraft.box.api.message.Placeholders.CURRENT;
import static net.okocraft.box.api.message.Placeholders.ITEM;
import static net.okocraft.box.api.message.Placeholders.PERMISSION;
import static net.okocraft.box.api.message.Placeholders.PLAYER_NAME;

public class GiveCommand extends AbstractCommand {

    private final MessageKey.Arg4<String, BoxItem, Integer, Integer> successSender;
    private final MessageKey.Arg4<String, BoxItem, Integer, Integer> successTarget;
    private final MessageKey.Arg1<BoxItem> noStock;
    private final MessageKey selfSpecified;
    private final MessageKey.Arg2<String, String> targetNoPermission;
    private final MessageKey.Arg1<String> targetCannotUse;
    private final MessageKey help;

    public GiveCommand(@NotNull DefaultMessageCollector collector) {
        super("give", "box.command.give", Set.of("g"));

        this.successSender = MessageKey.arg4(collector.add("box.command.box.give.success.sender", "<gray>Sent <aqua><item><gray>x<aqua><amount><gray> to player <aqua><player_name><gray> (Now <aqua><current><gray>)."), PLAYER_NAME, ITEM, AMOUNT, CURRENT);
        this.successTarget = MessageKey.arg4(collector.add("box.command.box.give.success.target", "<gray>Received <aqua><item><gray>x<aqua><amount><gray> from player <aqua><player_name><gray> (Now <aqua><current><gray>)."), PLAYER_NAME, ITEM, AMOUNT, CURRENT);
        this.noStock = MessageKey.arg1(collector.add("box.command.box.give.no-stock", "<red>Item <aqua><item><red> is out of stock."), ITEM);
        this.selfSpecified = MessageKey.key(collector.add("box.command.box.give.self", "<red>You can't send it to yourself."));
        this.targetNoPermission = MessageKey.arg2(collector.add("box.command.box.give.target-no-permission", "<red>Player <aqua><player_name><red> doesn't have the permission <aqua><permission><red>."), PLAYER_NAME, PERMISSION);
        this.targetCannotUse = MessageKey.arg1(collector.add("box.command.box.give.target-cannot-use-box", "<red>Player <aqua><player_name><red> cannot use Box."), PLAYER_NAME);
        this.help = MessageKey.key(collector.add("box.command.box.give.help", "<aqua>/box give <player> <item> [amount]<dark_gray> - <gray>Sends items to others"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ErrorMessages.COMMAND_ONLY_PLAYER);
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(ErrorMessages.NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(this.getHelp());
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(ErrorMessages.PLAYER_NOT_FOUND.apply(args[1]));
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(this.selfSpecified);
            return;
        }

        if (!target.hasPermission(this.getPermissionNode())) {
            sender.sendMessage(this.targetNoPermission.apply(target.getName(), this.getPermissionNode()));
            return;
        }

        if (!BoxAPI.api().canUseBox(target)) {
            sender.sendMessage(this.targetCannotUse.apply(target.getName()));
            return;
        }

        Optional<BoxItem> optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(args[2]);

        if (optionalBoxItem.isEmpty()) {
            sender.sendMessage(ErrorMessages.ITEM_NOT_FOUND.apply(args[2]));
            return;
        }

        BoxItem boxItem = optionalBoxItem.get();

        BoxPlayerMap playerMap = BoxAPI.api().getBoxPlayerMap();

        BoxPlayer senderBoxPlayer = playerMap.get(player);
        StockHolder stockHolder = senderBoxPlayer.getCurrentStockHolder();
        int currentStock = stockHolder.getAmount(boxItem);

        if (currentStock < 1) {
            sender.sendMessage(this.noStock.apply(boxItem));
            return;
        }

        int amount;

        if (3 < args.length) {
            try {
                amount = Math.min(currentStock, Math.max(Integer.parseInt(args[3]), 1));
            } catch (NumberFormatException e) {
                sender.sendMessage(ErrorMessages.INVALID_NUMBER.apply(args[3]));
                return;
            }
        } else {
            amount = 1;
        }

        if (!playerMap.isLoaded(target)) {
            if (playerMap.isScheduledLoading(player)) {
                sender.sendMessage(ErrorMessages.playerDataIsLoading(target.getName()));
            } else {
                sender.sendMessage(ErrorMessages.playerDataIsNotLoaded(target.getName()));
            }
            return;
        }

        BoxPlayer targetBoxPlayer = playerMap.get(target);

        int senderDecreaseResult = stockHolder.decreaseIfPossible(boxItem, amount, new CommandCauses.Give(targetBoxPlayer));

        if (senderDecreaseResult == -1) {
            sender.sendMessage(this.noStock.apply(boxItem));
            return;
        }

        int targetCurrent = targetBoxPlayer.getCurrentStockHolder().increase(boxItem, amount, new CommandCauses.Receive(senderBoxPlayer));

        sender.sendMessage(this.successSender.apply(target.getName(), boxItem, amount, senderDecreaseResult));
        target.sendMessage(this.successTarget.apply(sender.getName(), boxItem, amount, targetCurrent));
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            return TabCompleter.players(args[1], this.getPermissionNode());
        }

        if (args.length == 3) {
            String itemNameFilter = args[2].toLowerCase(Locale.ENGLISH);
            StockHolder stockHolder = BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder();

            return stockHolder.getStockedItems().stream()
                .map(BoxItem::getPlainName)
                .filter(itemName -> itemName.toLowerCase(Locale.ENGLISH).startsWith(itemNameFilter))
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }
}
