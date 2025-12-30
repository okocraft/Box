package net.okocraft.box.feature.command.box;

import dev.siroshun.mcmsgdef.MessageKey;
import dev.siroshun.mcmsgdef.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.event.player.PlayerCollectItemInfoEvent;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.api.util.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.okocraft.box.api.message.Placeholders.AMOUNT;
import static net.okocraft.box.api.message.Placeholders.ITEM;
import static net.okocraft.box.api.message.Placeholders.ITEM_NAME;

public class ItemInfoCommand extends AbstractCommand {

    private static final Placeholder<Integer> ITEM_INTERNAL_ID = id -> Argument.numeric("id", id);

    private final MessageKey isAir;
    private final MessageKey itemNotRegistered;
    private final MessageKey.Arg3<BoxItem, String, Integer> itemBasicInfo;
    private final MessageKey.Arg1<Integer> currentStock;
    private final MessageKey help;

    public ItemInfoCommand(@NotNull DefaultMessageCollector collector) {
        super("iteminfo", "box.command.iteminfo", Set.of("i"));

        this.isAir = MessageKey.key(collector.add("box.command.box.iteminfo.is-air", "<red>You have no item in your main hand."));
        this.itemNotRegistered = MessageKey.key(collector.add("box.command.box.iteminfo.item-not-registered", "<red>The item in your main hand is not registered."));
        this.itemBasicInfo = MessageKey.arg3(collector.add("box.command.box.iteminfo.basic", "<gray>Item <aqua><item><gray> (<aqua><item_name><gray>) <dark_gray>#<id>"), ITEM, ITEM_NAME, ITEM_INTERNAL_ID);
        this.currentStock = MessageKey.arg1(collector.add("box.command.box.iteminfo.stock", "<gray>Current stock: <aqua><amount>"), AMOUNT);
        this.help = MessageKey.key(collector.add("box.command.box.iteminfo.help", "<aqua>/box iteminfo [item]<dark_gray> - <gray>Shows information about the item you have or specify"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ErrorMessages.COMMAND_ONLY_PLAYER);
            return;
        }

        BoxItem boxItem;

        if (1 < args.length) {
            Optional<BoxItem> optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(args[1]);

            if (optionalBoxItem.isPresent()) {
                boxItem = optionalBoxItem.get();
            } else {
                sender.sendMessage(ErrorMessages.ITEM_NOT_FOUND.apply(args[1]));
                return;
            }
        } else {
            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

            if (itemInMainHand.getType().isAir()) {
                sender.sendMessage(this.isAir);
                return;
            }

            Optional<BoxItem> optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(itemInMainHand);

            if (optionalBoxItem.isPresent()) {
                boxItem = optionalBoxItem.get();
            } else {
                sender.sendMessage(this.itemNotRegistered);
                return;
            }
        }

        BoxPlayer boxPlayer = BoxAPI.api().getBoxPlayerMap().get(player);

        PlayerCollectItemInfoEvent event = new PlayerCollectItemInfoEvent(boxPlayer, boxItem);

        event.addInfo(this.itemBasicInfo.apply(boxItem, boxItem.getPlainName(), boxItem.getInternalId()));
        event.addInfo(this.currentStock.apply(boxPlayer.getCurrentStockHolder().getAmount(boxItem)));

        BoxAPI.api().getEventCallers().async().call(event, e -> {
            for (Component info : e.getInfo()) {
                e.getBoxPlayer().getPlayer().sendMessage(info);
            }
        });
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            return TabCompleter.itemNames(args[1]);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }
}
