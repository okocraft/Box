package net.okocraft.box.feature.command.box;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.feature.command.message.BoxMessage;
import net.okocraft.box.feature.command.shared.SharedStockListCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class StockListCommand extends AbstractCommand {

    public StockListCommand() {
        super("stocklist", "box.command.stocklist", Set.of("slist", "list", "sl", "l"));
    }

    @Override
    public @NotNull Component getHelp() {
        return BoxMessage.LIST_HELP;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        var playerMap = BoxProvider.get().getBoxPlayerMap();

        if (!playerMap.isLoaded(player)) {
            if (playerMap.isScheduledLoading(player)) {
                sender.sendMessage(GeneralMessage.ERROR_PLAYER_LOADING);
            } else {
                sender.sendMessage(GeneralMessage.ERROR_PLAYER_NOT_LOADED);
            }

            return;
        }

        var boxPlayer = playerMap.get(player);

        var context = 1 < args.length ? SharedStockListCommand.createContextFromArguments(Arrays.copyOfRange(args, 1, args.length)) : new SharedStockListCommand.Context();
        sender.sendMessage(SharedStockListCommand.createStockList(boxPlayer.getCurrentStockHolder(), context));
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            return SharedStockListCommand.getArgumentTypes();
        }

        int index = args.length - 1;
        return SharedStockListCommand.createTabCompletion(args[index - 1], args[index]);
    }
}
