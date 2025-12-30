package net.okocraft.box.feature.command.boxadmin.stock;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.okocraft.box.api.message.Placeholders.CURRENT;
import static net.okocraft.box.api.message.Placeholders.ITEM;
import static net.okocraft.box.api.message.Placeholders.PLAYER_NAME;

class StockInfoCommand extends AbstractCommand {

    private final MessageKey.Arg3<String, BoxItem, Integer> stockInfo;
    private final MessageKey help;

    StockInfoCommand(@NotNull DefaultMessageCollector collector) {
        super("info", "box.admin.command.stock.info", Set.of("i"));
        this.stockInfo = MessageKey.arg3(collector.add("box.command.boxadmin.stock.info.amount", "<gray>The amount of player <aqua><player_name><gray>'s item <aqua><item><gray> : <aqua><current>"), PLAYER_NAME, ITEM, CURRENT);
        this.help = MessageKey.key(collector.add("box.command.boxadmin.stock.info.help", "<aqua>/boxadmin stock info <player> <item name><dark_gray> - <gray>Shows the amount of stock"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ErrorMessages.NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(this.getHelp());
            return;
        }

        Optional<BoxItem> boxItem = BoxAPI.api().getItemManager().getBoxItem(args[3]);

        if (boxItem.isEmpty()) {
            sender.sendMessage(ErrorMessages.ITEM_NOT_FOUND.apply(args[3]));
            return;
        }

        BoxUser target = UserSearcher.search(args[2]);

        if (target != null) {
            sender.sendMessage(this.stockInfo.apply(
                target.getName().orElseGet(target.getUUID()::toString),
                boxItem.get(),
                BoxAPI.api().getStockManager().getPersonalStockHolder(target).getAmount(boxItem.get())
            ));
        } else {
            sender.sendMessage(ErrorMessages.PLAYER_NOT_FOUND.apply(args[2]));
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
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }
}
