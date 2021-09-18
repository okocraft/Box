package net.okocraft.box.feature.command.box;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.feature.command.message.BoxMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

        var item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            player.sendMessage(BoxMessage.ITEM_INFO_IS_AIR);
            return;
        }

        var boxItem = BoxProvider.get().getItemManager().getBoxItem(item);

        if (boxItem.isEmpty()) {
            player.sendMessage(BoxMessage.ITEM_INFO_NOT_REGISTERED);
            return;
        }

        int stock = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder().getAmount(boxItem.get());

        player.sendMessage(BoxMessage.ITEM_INFO_NAME.apply(boxItem.get()));
        player.sendMessage(BoxMessage.ITEM_INFO_STOCK.apply(stock));
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxMessage.ITEM_INFO_HELP;
    }
}
