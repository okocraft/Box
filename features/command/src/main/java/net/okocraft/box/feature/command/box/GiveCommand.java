package net.okocraft.box.feature.command.box;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.arg.Arg2;
import com.github.siroshun09.messages.minimessage.arg.Arg4;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.arg.Arg2.arg2;
import static com.github.siroshun09.messages.minimessage.arg.Arg4.arg4;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
import static net.okocraft.box.api.message.Placeholders.AMOUNT;
import static net.okocraft.box.api.message.Placeholders.CURRENT;
import static net.okocraft.box.api.message.Placeholders.ITEM;
import static net.okocraft.box.api.message.Placeholders.PERMISSION;
import static net.okocraft.box.api.message.Placeholders.PLAYER_NAME;

public class GiveCommand extends AbstractCommand {

    private final Arg4<String, BoxItem, Integer, Integer> successSender;
    private final Arg4<String, BoxItem, Integer, Integer> successTarget;
    private final Arg1<BoxItem> noStock;
    private final MiniMessageBase selfSpecified;
    private final Arg2<String, String> targetNoPermission;
    private final Arg1<String> targetCannotUse;
    private final MiniMessageBase help;

    public GiveCommand(@NotNull DefaultMessageCollector collector) {
        super("give", "box.command.give", Set.of("g"));

        this.successSender = arg4(collector.add("box.command.box.give.success.sender", "<gray>Sent <aqua><item><gray>x<aqua><amount><gray> to player <aqua><player_name><gray> (Now <aqua><current><gray>)."), PLAYER_NAME, ITEM, AMOUNT, CURRENT);
        this.successTarget = arg4(collector.add("box.command.box.give.success.target", "<gray>Received <aqua><item><gray>x<aqua><amount><gray> from player <aqua><player_name><gray> (Now <aqua><current><gray>)."), PLAYER_NAME, ITEM, AMOUNT, CURRENT);
        this.noStock = arg1(collector.add("box.command.box.give.no-stock", "<red>Item <aqua><item><red> is out of stock."), ITEM);
        this.selfSpecified = messageKey(collector.add("box.command.box.give.self", "<red>You can't send it to yourself."));
        this.targetNoPermission = arg2(collector.add("box.command.box.give.target-no-permission", "<red>Player <aqua><player_name><red> doesn't have the permission <aqua><permission><red>."), PLAYER_NAME, PERMISSION);
        this.targetCannotUse = arg1(collector.add("box.command.box.give.target-cannot-use-box", "<red>Player <aqua><player_name><red> cannot use Box."), PLAYER_NAME);
        this.help = messageKey(collector.add("box.command.box.give.help", "<aqua>/box give <player> <item> [amount]<dark_gray> - <gray>Sends items to others"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (!(sender instanceof Player player)) {
            ErrorMessages.COMMAND_ONLY_PLAYER.source(msgSrc).send(sender);
            return;
        }

        if (args.length < 3) {
            ErrorMessages.NOT_ENOUGH_ARGUMENT.source(msgSrc).send(sender);
            sender.sendMessage(this.getHelp(msgSrc));
            return;
        }

        var target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            ErrorMessages.PLAYER_NOT_FOUND.apply(args[1]).source(msgSrc).send(sender);
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            this.selfSpecified.source(msgSrc).send(sender);
            return;
        }

        if (!target.hasPermission(getPermissionNode())) {
            this.targetNoPermission.apply(target.getName(), this.getPermissionNode()).source(msgSrc).send(sender);
            return;
        }

        if (!BoxAPI.api().canUseBox(target)) {
            this.targetCannotUse.apply(target.getName()).source(msgSrc).send(sender);
            return;
        }

        var optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(args[2]);

        if (optionalBoxItem.isEmpty()) {
            ErrorMessages.ITEM_NOT_FOUND.apply(args[2]).source(msgSrc).send(sender);
            return;
        }

        var boxItem = optionalBoxItem.get();

        var playerMap = BoxAPI.api().getBoxPlayerMap();

        var senderBoxPlayer = playerMap.get(player);
        var stockHolder = senderBoxPlayer.getCurrentStockHolder();
        var currentStock = stockHolder.getAmount(boxItem);

        if (currentStock < 1) {
            this.noStock.apply(boxItem);
            return;
        }

        int amount;

        if (3 < args.length) {
            try {
                amount = Math.min(currentStock, Math.max(Integer.parseInt(args[3]), 1));
            } catch (NumberFormatException e) {
                ErrorMessages.INVALID_NUMBER.apply(args[3]).source(msgSrc).send(sender);
                return;
            }
        } else {
            amount = 1;
        }

        if (!playerMap.isLoaded(target)) {
            if (playerMap.isScheduledLoading(player)) {
                ErrorMessages.playerDataIsLoading(target.getName()).source(msgSrc).send(sender);
            } else {
                ErrorMessages.playerDataIsNotLoaded(target.getName()).source(msgSrc).send(sender);
            }
            return;
        }

        var targetBoxPlayer = playerMap.get(target);

        int senderDecreaseResult = stockHolder.decreaseIfPossible(boxItem, amount, new CommandCauses.Give(targetBoxPlayer));

        if (senderDecreaseResult == -1) {
            this.noStock.apply(boxItem).source(msgSrc).send(sender);
            return;
        }

        var targetCurrent = targetBoxPlayer.getCurrentStockHolder().increase(boxItem, amount, new CommandCauses.Receive(senderBoxPlayer));

        this.successSender.apply(target.getName(), boxItem, amount, senderDecreaseResult).source(msgSrc).send(sender);
        this.successTarget.apply(sender.getName(), boxItem, amount, targetCurrent)
                .source(BoxAPI.api().getMessageProvider().findSource(target))
                .send(target);
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
            var itemNameFilter = args[2].toLowerCase(Locale.ENGLISH);
            var stockHolder = BoxAPI.api().getBoxPlayerMap().get(player).getCurrentStockHolder();

            return stockHolder.getStockedItems().stream()
                    .map(BoxItem::getPlainName)
                    .filter(itemName -> itemName.toLowerCase(Locale.ENGLISH).startsWith(itemNameFilter))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }
}
