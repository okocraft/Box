package net.okocraft.box.feature.command.boxadmin.stock;

import com.github.siroshun09.messages.minimessage.arg.Arg3;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.okocraft.box.api.message.Placeholders.CURRENT;
import static net.okocraft.box.api.message.Placeholders.ITEM;
import static net.okocraft.box.api.message.Placeholders.PLAYER_NAME;

class StockInfoCommand extends AbstractCommand {

    private final Arg3<String, BoxItem, Integer> stockInfo;
    private final MiniMessageBase help;

    StockInfoCommand(@NotNull DefaultMessageCollector collector) {
        super("info", "box.admin.command.stock.info", Set.of("i"));
        this.stockInfo = Arg3.arg3(collector.add("box.command.boxadmin.stock.info.amount", "<gray>The amount of player <aqua><player_name><gray>'s item <aqua><item><gray> : <aqua><current>"), PLAYER_NAME, ITEM, CURRENT);
        this.help = MiniMessageBase.messageKey(collector.add("box.command.boxadmin.stock.info.help", "<aqua>/boxadmin stock info <player> <item name><dark_gray> - <gray>Shows the amount of stock"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (args.length < 4) {
            ErrorMessages.NOT_ENOUGH_ARGUMENT.source(msgSrc).send(sender);
            sender.sendMessage(this.getHelp(msgSrc));
            return;
        }

        var boxItem = BoxAPI.api().getItemManager().getBoxItem(args[3]);

        if (boxItem.isEmpty()) {
            ErrorMessages.ITEM_NOT_FOUND.apply(args[3]).source(msgSrc).send(sender);
            return;
        }

        var target = UserSearcher.search(args[2]);

        if (target != null) {
            this.stockInfo.apply(
                target.getName().orElseGet(target.getUUID()::toString),
                boxItem.get(),
                BoxAPI.api().getStockManager().getPersonalStockHolder(target).getAmount(boxItem.get())
            ).source(msgSrc).send(sender);
        } else {
            ErrorMessages.PLAYER_NOT_FOUND.apply(args[2]).source(msgSrc).send(sender);
        }
    }

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
}
