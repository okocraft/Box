package net.okocraft.box.feature.command.boxadmin.stock;

import com.github.siroshun09.messages.minimessage.arg.Arg4;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.github.siroshun09.messages.minimessage.arg.Arg4.arg4;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
import static net.okocraft.box.api.message.Placeholders.AMOUNT;
import static net.okocraft.box.api.message.Placeholders.CURRENT;
import static net.okocraft.box.api.message.Placeholders.ITEM;
import static net.okocraft.box.api.message.Placeholders.PLAYER_NAME;

abstract class StockModifyCommands extends AbstractCommand {

    private final boolean allowZero;
    private final Arg4<String, BoxItem, Integer, Integer> successSender;
    private final Arg4<String, BoxItem, Integer, Integer> successTarget;
    private final MiniMessageBase help;

    private StockModifyCommands(@NotNull String name, @NotNull Set<String> aliases,
                                boolean allowZero,
                                @NotNull Arg4<String, BoxItem, Integer, Integer> successSender,
                                @NotNull Arg4<String, BoxItem, Integer, Integer> successTarget,
                                @NotNull MiniMessageBase help) {
        super(name, "box.admin.command.stock." + name, aliases);
        this.allowZero = allowZero;
        this.successSender = successSender;
        this.successTarget = successTarget;
        this.help = help;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (args.length < 5) {
            ErrorMessages.NOT_ENOUGH_ARGUMENT.source(msgSrc).send(sender);
            sender.sendMessage(this.getHelp(msgSrc));
            return;
        }

        var item = BoxAPI.api().getItemManager().getBoxItem(args[3]);

        if (item.isEmpty()) {
            ErrorMessages.ITEM_NOT_FOUND.apply(args[3]).source(msgSrc).send(sender);
            return;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            ErrorMessages.INVALID_NUMBER.apply(args[4]).source(msgSrc).send(sender);
            return;
        }

        if (amount <= 0) {
            if (amount != 0 || !this.allowZero) {
                ErrorMessages.INVALID_NUMBER.apply(args[4]).source(msgSrc).send(sender);
                return;
            }
        }

        var target = UserSearcher.search(args[2]);

        if (target == null) {
            ErrorMessages.PLAYER_NOT_FOUND.apply(args[2]).source(msgSrc).send(sender);
            return;
        }

        var stockHolder = BoxAPI.api().getStockManager().getPersonalStockHolder(target);
        int current = this.modifyStock(sender, stockHolder, item.get(), amount);

        this.successSender.apply(target.getName().orElseGet(target.getUUID()::toString), item.get(), amount, current).source(msgSrc).send(sender);

        var targetPlayer = Bukkit.getPlayer(target.getUUID());
        if (targetPlayer != null && sender != targetPlayer) {
            var targetSource = BoxAPI.api().getMessageProvider().findSource(targetPlayer);
            this.successTarget.apply(sender.getName(), item.get(), amount, current).source(targetSource).send(targetPlayer);
        }
    }

    protected abstract int modifyStock(@NotNull CommandSender sender, @NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount);

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 3) {
            return TabCompleter.players(args[2]);
        }

        if (args.length == 4) {
            return TabCompleter.itemNames(args[3]);
        }

        return Collections.emptyList();
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }

    static class StockGiveCommand extends StockModifyCommands {

        StockGiveCommand(@NotNull DefaultMessageCollector collector) {
            super(
                    "give",
                    Set.of("g"),
                    false,
                    arg4(collector.add("box.command.boxadmin.stock.give.success.sender", "<gray>Gave <aqua><item><gray>x<aqua><amount><gray> to player <aqua><player_name><gray> (Now <aqua><current><gray>)."), PLAYER_NAME, ITEM, AMOUNT, CURRENT),
                    arg4(collector.add("box.command.boxadmin.stock.give.success.target", "<aqua><player_name><gray> gave you <aqua><item><gray>x<aqua><amount><gray> (Now <aqua><current><gray>)."), PLAYER_NAME, ITEM, AMOUNT, CURRENT),
                    messageKey(collector.add("box.command.boxadmin.stock.give.help", "<aqua>/boxadmin give <player> <item> <amount><dark_gray> - <gray>Increases stock"))
            );
        }

        @Override
        protected int modifyStock(@NotNull CommandSender sender, @NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
            return stockHolder.increase(item, amount, new CommandCauses.AdminGive(sender));
        }
    }

    static class StockTakeCommand extends StockModifyCommands {

        StockTakeCommand(@NotNull DefaultMessageCollector collector) {
            super(
                    "take",
                    Set.of("t"),
                    false,
                    arg4(collector.add("box.command.boxadmin.stock.take.success.sender", "<gray>Took <aqua><item><gray>x<aqua><amount><gray> from player <aqua><player_name><gray> (Now <aqua><current><gray>)."), PLAYER_NAME, ITEM, AMOUNT, CURRENT),
                    arg4(collector.add("box.command.boxadmin.stock.take.success.target", "<aqua><player_name><gray> took <aqua><item><gray>x<aqua><amount><gray> from you (Now <aqua><current><gray>)."), PLAYER_NAME, ITEM, AMOUNT, CURRENT),
                    messageKey(collector.add("box.command.boxadmin.stock.take.help", "<aqua>/boxadmin take <player> <item name> <amount><dark_gray> - <gray>Decreases stock"))
            );
        }

        @Override
        protected int modifyStock(@NotNull CommandSender sender, @NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
            return stockHolder.decrease(item, amount, new CommandCauses.AdminTake(sender));
        }
    }

    static class StockSetCommand extends StockModifyCommands {

        StockSetCommand(@NotNull DefaultMessageCollector collector) {
            super(
                    "set",
                    Set.of("s"),
                    true,
                    arg4(collector.add("box.command.boxadmin.stock.set.success.sender", "<gray>Player <aqua><player_name><gray>'s item <aqua><item><gray> has been set to <aqua><amount><gray>."), PLAYER_NAME, ITEM, AMOUNT, CURRENT),
                    arg4(collector.add("box.command.boxadmin.stock.set.success.target", "<gray>Item <aqua><item><gray> has been set to <aqua><amount><gray> by <aqua><player_name><gray>."), PLAYER_NAME, ITEM, AMOUNT, CURRENT),
                    messageKey(collector.add("box.command.boxadmin.stock.set.help", "<aqua>/boxadmin set <player> <item name> <amount><dark_gray> - <gray>Sets stock"))
            );
        }

        @Override
        protected int modifyStock(@NotNull CommandSender sender, @NotNull StockHolder stockHolder, @NotNull BoxItem item, int amount) {
            stockHolder.setAmount(item, amount, new CommandCauses.AdminSet(sender));
            return amount;
        }
    }
}
