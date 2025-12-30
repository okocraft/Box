package net.okocraft.box.feature.command.box;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.feature.command.shared.SharedStockListCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class StockListCommand extends AbstractCommand {

    private final SharedStockListCommand sharedStockListCommand;
    private final MessageKey help;

    public StockListCommand(@NotNull DefaultMessageCollector collector, @NotNull SharedStockListCommand sharedStockListCommand) {
        super("stocklist", "box.command.stocklist", Set.of("slist", "list", "sl", "l"));
        this.sharedStockListCommand = sharedStockListCommand;
        this.help = MessageKey.key(collector.add("box.command.box.stocklist.help", "<aqua>/box stocklist [args]<dark_gray> - <gray>Shows the stock list"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ErrorMessages.COMMAND_ONLY_PLAYER);
            return;
        }

        BoxPlayerMap playerMap = BoxAPI.api().getBoxPlayerMap();

        if (playerMap.isLoaded(player)) {
            this.sharedStockListCommand.createAndSendStockList(
                sender,
                playerMap.get(player).getCurrentStockHolder(),
                1 < args.length ? Arrays.copyOfRange(args, 1, args.length) : null
            );
        } else {
            if (playerMap.isScheduledLoading(player)) {
                sender.sendMessage(ErrorMessages.playerDataIsLoading(null));
            } else {
                sender.sendMessage(ErrorMessages.playerDataIsNotLoaded(null));
            }
        }
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

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help.asComponent().appendNewline().append(this.sharedStockListCommand.getArgHelp());
    }
}
