package net.okocraft.box.feature.command.boxadmin;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class InfinityCommand extends AbstractCommand {

    private final MiniMessageBase enableSelf;
    private final Arg1<String> enableOtherSender;
    private final Arg1<String> enableOtherTarget;
    private final MiniMessageBase disableSelf;
    private final Arg1<String> disableOtherSender;
    private final Arg1<String> disableOtherTarget;
    private final MiniMessageBase tip;
    private final MiniMessageBase help;

    public InfinityCommand(@NotNull DefaultMessageCollector collector) {
        super("infinity", "box.admin.command.infinity", Set.of("i", "inf"));
        this.enableSelf = messageKey(collector.add("box.command.boxadmin.infinity.enable.self", "<gray>Infinite stock mode has been <green>enabled<gray>."));
        this.enableOtherSender = arg1(collector.add("box.command.boxadmin.infinity.enable.other.sender", "<green>Enabled<gray> infinite stock mode for player <aqua><player_name><gray>."), Placeholders.PLAYER_NAME);
        this.enableOtherTarget = arg1(collector.add("box.command.boxadmin.infinity.enable.other.target", "<gray>Infinite stock mode has been <green>enabled<gray> by <aqua><player_name><gray>."), Placeholders.PLAYER_NAME);
        this.disableSelf = messageKey(collector.add("box.command.boxadmin.infinity.disable.self", "<gray>Infinite stock mode has been <green>disabled<gray>."));
        this.disableOtherSender = arg1(collector.add("box.command.boxadmin.infinity.disable.other.sender", "<green>Disabled<gray> infinite stock mode for player <aqua><player_name><gray>."), Placeholders.PLAYER_NAME);
        this.disableOtherTarget = arg1(collector.add("box.command.boxadmin.infinity.disable.other.target", "<gray>Infinite stock mode has been <green>disabled<gray> by <aqua><player_name><gray>."), Placeholders.PLAYER_NAME);
        this.tip = messageKey(collector.add("box.command.boxadmin.infinity.tip", "<gray>Infinite stock mode does not save deposited items."));
        this.help = MiniMessageBase.messageKey(collector.add("box.command.boxadmin.infinity.help", "<aqua>/boxadmin infinity [player]<dark_gray> - <gray>Toggles infinite stock mode"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        Player target;
        boolean self;

        if (1 < args.length) {
            target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                ErrorMessages.PLAYER_NOT_FOUND.apply(args[1]).source(msgSrc).send(sender);
                return;
            }

            self = false;
        } else {
            if (sender instanceof Player player) {
                target = player;
            } else {
                ErrorMessages.NOT_ENOUGH_ARGUMENT.source(msgSrc).send(sender);
                sender.sendMessage(this.getHelp(msgSrc));
                return;
            }

            self = true;
        }

        var playerMap = BoxAPI.api().getBoxPlayerMap();

        if (!playerMap.isLoaded(target)) {
            if (playerMap.isScheduledLoading(target)) {
                ErrorMessages.playerDataIsLoading(self ? null : target.getName()).source(msgSrc).send(sender);
            } else {
                ErrorMessages.playerDataIsNotLoaded(self ? null : target.getName()).source(msgSrc).send(sender);
            }
            return;
        }

        var boxPlayer = playerMap.get(target);

        boolean enabled;

        if (boxPlayer.getCurrentStockHolder() instanceof InfinityStockHolder) {
            boxPlayer.setCurrentStockHolder(boxPlayer.getPersonalStockHolder());
            enabled = false;
        } else {
            boxPlayer.setCurrentStockHolder(new InfinityStockHolder());
            enabled = true;
        }

        var targetMsgSrc = BoxAPI.api().getMessageProvider().findSource(target);

        if (self) {
            (enabled ? this.enableSelf : this.disableSelf).source(msgSrc).send(sender);
        } else {
            (enabled ? this.enableOtherSender : this.disableOtherSender).apply(target.getName()).source(msgSrc).send(sender);
            (enabled ? this.enableOtherTarget : this.disableOtherTarget).apply(sender.getName()).source(msgSrc).send(target);
        }

        if (enabled) {
            this.tip.source(targetMsgSrc).send(target);
        }
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }

    private static class InfinityStockHolder implements StockHolder {

        private final UUID uuid = UUID.randomUUID();

        @Override
        public @NotNull String getName() {
            return "infinity";
        }

        @Override
        public @NotNull UUID getUUID() {
            return uuid;
        }

        @Override
        public int getAmount(int itemId) {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setAmount(@NotNull BoxItem item, int amount, @NotNull StockEvent.Cause cause) {
        }

        @Override
        public int increase(@NotNull BoxItem item, int increment, @NotNull StockEvent.Cause cause) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int decrease(@NotNull BoxItem item, int decrement, @NotNull StockEvent.Cause cause) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int decreaseToZero(@NotNull BoxItem item, int limit, @NotNull StockEvent.Cause cause) {
            return limit;
        }

        @Override
        public int decreaseIfPossible(@NotNull BoxItem item, int decrement, @NotNull StockEvent.Cause cause) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean decreaseIfPossible(@NotNull Object2IntMap<BoxItem> decrementMap, @NotNull StockEvent.Cause cause) {
            return true;
        }

        @Override
        public @NotNull @Unmodifiable Collection<BoxItem> getStockedItems() {
            return BoxAPI.api().getItemManager().getItemList();
        }

        @Override
        public @NotNull @Unmodifiable Collection<StockData> toStockDataCollection() {
            return Collections.emptyList();
        }

        @Override
        public @NotNull @Unmodifiable Collection<StockData> reset() {
            return Collections.emptyList();
        }
    }
}
