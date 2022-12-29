package net.okocraft.box.feature.command.boxadmin.stock;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserStockHolderOperator;
import net.okocraft.box.feature.command.message.BoxAdminMessage;
import net.okocraft.box.feature.command.event.stock.CommandCauses;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class StockResetCommand extends AbstractCommand {

    StockResetCommand() {
        super("reset", "box.admin.command.stock.reset", Set.of("r"));
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxAdminMessage.STOCK_RESET_HELP;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 4) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(getHelp());
            return;
        }

        var item = BoxProvider.get().getItemManager().getBoxItem(args[3]);

        if (item.isEmpty()) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[3]));
            return;
        }

        UserStockHolderOperator.create(args[2])
                .supportOffline(true)
                .stockHolderOperator(target -> {
                    target.setAmount(item.get(), 0, new CommandCauses.AdminReset(sender));
                    sendMessage(sender, Bukkit.getPlayer(target.getUUID()), target.getName(), item.get());
                })
                .onNotFound(name -> sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(name)))
                .run();
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

    private void sendMessage(@NotNull CommandSender sender, @Nullable Player targetPlayer, @NotNull String targetName, BoxItem item) {
        sender.sendMessage(BoxAdminMessage.STOCK_RESET_SUCCESS_SENDER.apply(targetName, item));

        if (targetPlayer != null && !sender.getName().equals(targetName)) {
            targetPlayer.sendMessage(BoxAdminMessage.STOCK_RESET_SUCCESS_TARGET.apply(sender.getName(), item));
        }
    }
}
