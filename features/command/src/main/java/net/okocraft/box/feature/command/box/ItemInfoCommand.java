package net.okocraft.box.feature.command.box;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.arg.Arg3;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.base.Placeholder;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.event.player.PlayerCollectItemInfoEvent;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.arg.Arg3.arg3;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
import static net.okocraft.box.api.message.Placeholders.AMOUNT;
import static net.okocraft.box.api.message.Placeholders.ITEM;
import static net.okocraft.box.api.message.Placeholders.ITEM_NAME;

public class ItemInfoCommand extends AbstractCommand {

    private static final Placeholder<Integer> ITEM_INTERNAL_ID = Placeholder.component("id", Component::text);

    private final MiniMessageBase isAir;
    private final MiniMessageBase itemNotRegistered;
    private final Arg3<BoxItem, String, Integer> itemBasicInfo;
    private final Arg1<Integer> currentStock;
    private final MiniMessageBase help;

    public ItemInfoCommand(@NotNull DefaultMessageCollector collector) {
        super("iteminfo", "box.command.iteminfo", Set.of("i"));

        this.isAir = messageKey(collector.add("box.command.box.iteminfo.is-air", "<red>You have no item in your main hand."));
        this.itemNotRegistered = messageKey(collector.add("box.command.box.iteminfo.item-not-registered", "<red>The item in your main hand is not registered."));
        this.itemBasicInfo = arg3(collector.add("box.command.box.iteminfo.basic", "<gray>Item <aqua><item><gray> (<aqua><item_name><gray>) <dark_gray>#<id>"), ITEM, ITEM_NAME, ITEM_INTERNAL_ID);
        this.currentStock = arg1(collector.add("box.command.box.iteminfo.stock", "<gray>Current stock: <aqua><amount>"), AMOUNT);
        this.help = messageKey(collector.add("box.command.box.iteminfo.help", "<aqua>/box iteminfo [item]<dark_gray> - <gray>Shows information about the item you have or specify"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (!(sender instanceof Player player)) {
            ErrorMessages.COMMAND_ONLY_PLAYER.source(msgSrc).send(sender);
            return;
        }

        BoxItem boxItem;

        if (1 < args.length) {
            var optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(args[1]);

            if (optionalBoxItem.isPresent()) {
                boxItem = optionalBoxItem.get();
            } else {
                ErrorMessages.ITEM_NOT_FOUND.apply(args[1]).source(msgSrc).send(sender);
                return;
            }
        } else {
            var itemInMainHand = player.getInventory().getItemInMainHand();

            if (itemInMainHand.getType().isAir()) {
                this.isAir.source(msgSrc).send(sender);
                return;
            }

            var optionalBoxItem = BoxAPI.api().getItemManager().getBoxItem(itemInMainHand);

            if (optionalBoxItem.isPresent()) {
                boxItem = optionalBoxItem.get();
            } else {
                this.itemNotRegistered.source(msgSrc).send(sender);
                return;
            }
        }

        var boxPlayer = BoxAPI.api().getBoxPlayerMap().get(player);

        var event = new PlayerCollectItemInfoEvent(boxPlayer, boxItem);

        event.addInfo(this.itemBasicInfo.apply(boxItem, boxItem.getPlainName(), boxItem.getInternalId()).source(msgSrc).message());
        event.addInfo(this.currentStock.apply(boxPlayer.getCurrentStockHolder().getAmount(boxItem)).source(msgSrc).message());

        BoxAPI.api().getEventCallers().async().call(event, e -> {
            for (var info : e.getInfo()) {
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
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }
}
