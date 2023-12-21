package net.okocraft.box.feature.command.box;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.event.player.PlayerCollectItemInfoEvent;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.feature.command.message.BoxMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ItemInfoCommand extends AbstractCommand {

    public ItemInfoCommand() {
        super("iteminfo", "box.command.iteminfo", Set.of("i"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        BoxItem boxItem;

        if (1 < args.length) {
            var optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(args[1]);

            if (optionalBoxItem.isPresent()) {
                boxItem = optionalBoxItem.get();
            } else {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[1]));
                return;
            }
        } else {
            var itemInMainHand = player.getInventory().getItemInMainHand();

            if (itemInMainHand.getType().isAir()) {
                player.sendMessage(BoxMessage.ITEM_INFO_IS_AIR);
                return;
            }

            var optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(itemInMainHand);

            if (optionalBoxItem.isPresent()) {
                boxItem = optionalBoxItem.get();
            } else {
                player.sendMessage(BoxMessage.ITEM_INFO_NOT_REGISTERED);
                return;
            }
        }

        var boxPlayer = BoxAPI.api().getBoxPlayerMap().get(player);

        var event = new PlayerCollectItemInfoEvent(boxPlayer, boxItem);

        event.addInfo(BoxMessage.ITEM_INFO_NAME.apply(boxItem));
        event.addInfo(BoxMessage.ITEM_INFO_ID.apply(boxItem.getPlainName()));
        event.addInfo(BoxMessage.ITEM_INFO_STOCK.apply(boxPlayer.getCurrentStockHolder().getAmount(boxItem)));

        BoxAPI.api().getEventManager().call(event);

        for (var info : event.getInfo()) {
            player.sendMessage(info);
        }
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxMessage.ITEM_INFO_HELP;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return TabCompleter.itemNames(args[1]);
        } else {
            return Collections.emptyList();
        }
    }
}
