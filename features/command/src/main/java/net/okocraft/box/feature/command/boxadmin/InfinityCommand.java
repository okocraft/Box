package net.okocraft.box.feature.command.boxadmin;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class InfinityCommand extends AbstractCommand {

    private static final StockHolder INFINITY_STOCK_HOLDER = new InfinityStockHolder();

    public InfinityCommand() {
        super("infinity", "box.admin.command.infinity", Set.of("i", "inf"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        BoxPlayer boxPlayer;

        if (1 < args.length) {
            var target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[1]));
                return;
            } else {
                boxPlayer = BoxProvider.get().getBoxPlayerMap().get(target);
            }
        } else {
            if (sender instanceof Player player) {
                boxPlayer = BoxProvider.get().getBoxPlayerMap().get(player);
            } else {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
                sender.sendMessage(getHelp());
                return;
            }
        }

        boolean enabled;

        if (boxPlayer.getCurrentStockHolder() == INFINITY_STOCK_HOLDER) {
            boxPlayer.setCurrentStockHolder(boxPlayer.getUserStockHolder());
            enabled = false;
        } else {
            boxPlayer.setCurrentStockHolder(INFINITY_STOCK_HOLDER);
            enabled = true;
        }

        var target = boxPlayer.getPlayer();

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

        @Override
        public @NotNull String getName() {
            return "infinity";
        }

        @Override
        public int getAmount(@NotNull BoxItem item) {
            return Integer.MAX_VALUE;
        }

        @Override
        public void setAmount(@NotNull BoxItem item, int amount) {
        }

        @Override
        public int increase(@NotNull BoxItem item) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int increase(@NotNull BoxItem item, int increment) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int decrease(@NotNull BoxItem item) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int decrease(@NotNull BoxItem item, int decrement) {
            return Integer.MAX_VALUE;
        }

        @Override
        public @NotNull @Unmodifiable Collection<BoxItem> getStockedItems() {
            return BoxProvider.get().getItemManager().getBoxItemSet();
        }

        @Override
        public @NotNull @Unmodifiable Collection<StockData> toStockDataCollection() {
            return Collections.emptyList();
        }
    }
}
