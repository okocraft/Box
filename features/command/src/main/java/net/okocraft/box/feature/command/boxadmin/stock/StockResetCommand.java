package net.okocraft.box.feature.command.boxadmin.stock;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class StockResetCommand extends AbstractCommand {

    private final MessageKey.Arg2<String, BoxItem> successSender;
    private final MessageKey.Arg2<String, BoxItem> successTarget;
    private final MessageKey help;

    StockResetCommand(@NotNull DefaultMessageCollector collector) {
        super("reset", "box.admin.command.stock.reset", Set.of("r"));
        this.successSender = MessageKey.arg2(collector.add("box.command.boxadmin.stock.reset.success.sender", "<gray>Player <aqua><player_name><gray>'s item <aqua><item><gray> has been reset."), Placeholders.PLAYER_NAME, Placeholders.ITEM);
        this.successTarget = MessageKey.arg2(collector.add("box.command.boxadmin.stock.reset.success.target", "<gray>Item <aqua><item><gray> has been reset by <aqua><player_name><gray>."), Placeholders.PLAYER_NAME, Placeholders.ITEM);
        this.help = MessageKey.key(collector.add("box.command.boxadmin.stock.reset.help", "<aqua>/boxadmin reset <player> <item name><dark_gray> - <gray>Resets stock"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length <= 3) {
            sender.sendMessage(ErrorMessages.NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(this.getHelp());
            return;
        }

        var item = BoxAPI.api().getItemManager().getBoxItem(args[3]);

        if (item.isEmpty()) {
            sender.sendMessage(ErrorMessages.ITEM_NOT_FOUND.apply(args[3]));
            return;
        }

        BoxUser target = UserSearcher.search(args[2]);
        if (target != null) {
            BoxAPI.api().getStockManager().getPersonalStockHolder(target).setAmount(item.get(), 0, new CommandCauses.AdminReset(sender));
            sender.sendMessage(this.successSender.apply(target.getName().orElseGet(target.getUUID()::toString), item.get()));

            Player targetPlayer = Bukkit.getPlayer(target.getUUID());
            if (targetPlayer != null && sender != targetPlayer) {
                targetPlayer.sendMessage(this.successTarget.apply(sender.getName(), item.get()));
            }
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
