package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.event.stockholder.stock.StockEvent;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class InfinityCommand extends AbstractCommand {

    public InfinityCommand() {
        super("infinity", "box.admin.command.infinity", Set.of("i", "inf"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        Player target;
        boolean self;

        if (1 < args.length) {
            target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[1]));
                return;
            }

            self = false;
        } else {
            if (sender instanceof Player player) {
                target = player;
            } else {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
                sender.sendMessage(getHelp());
                return;
            }

            self = true;
        }

        var playerMap = BoxProvider.get().getBoxPlayerMap();

        if (!playerMap.isLoaded(target)) {
            Component message;

            if (playerMap.isScheduledLoading(target)) {
                message = self ? GeneralMessage.ERROR_PLAYER_LOADING : GeneralMessage.ERROR_TARGET_PLAYER_LOADING.apply(target);
            } else {
                message = self ? GeneralMessage.ERROR_PLAYER_NOT_LOADED : GeneralMessage.ERROR_TARGET_PLAYER_NOT_LOADED.apply(target);
            }

            sender.sendMessage(message);
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

        if (sender instanceof Player player && player.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(BoxAdminMessage.INFINITY_MODE_TOGGLE.apply(enabled));
        } else {
            sender.sendMessage(BoxAdminMessage.INFINITY_MODE_TOGGLE_SENDER.apply(target, enabled));
            target.sendMessage(BoxAdminMessage.INFINITY_MODE_TOGGLE_TARGET.apply(sender, enabled));
        }

        if (enabled) {
            target.sendMessage(BoxAdminMessage.INFINITY_MODE_TIP);
        }
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.INFINITY_HELP;
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
        public int getAmount(@NotNull BoxItem item) {
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
        public @NotNull @Unmodifiable Collection<BoxItem> getStockedItems() {
            return BoxProvider.get().getItemManager().getItemList();
        }

        @Override
        public @NotNull @Unmodifiable Collection<StockData> toStockDataCollection() {
            return Collections.emptyList();
        }

        @Override
        public @NotNull Stream<StockData> stockDataStream() {
            return Stream.empty();
        }

        @Override
        public void reset() {
        }
    }
}
