package net.okocraft.box.feature.command.boxadmin.stock;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import net.okocraft.box.feature.command.shared.SharedStockListCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class StockListCommand extends AbstractCommand {

    private final SharedStockListCommand sharedStockListCommand;
    private final MiniMessageBase help;

    StockListCommand(@NotNull DefaultMessageCollector collector, @NotNull SharedStockListCommand sharedStockListCommand) {
        super("list", "box.admin.command.stock.list", Set.of("l"));
        this.sharedStockListCommand = sharedStockListCommand;
        this.help = MiniMessageBase.messageKey(collector.add("box.command.boxadmin.stock.list.help", "<aqua>/boxadmin stock list <player> [args]<dark_gray> - <gray>Shows the stock list"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (args.length <= 2) {
            ErrorMessages.NOT_ENOUGH_ARGUMENT.source(msgSrc).send(sender);
            sender.sendMessage(this.getHelp(msgSrc));
            return;
        }

        var target = UserSearcher.search(args[2]);

        if (target != null) {
            this.sharedStockListCommand.createAndSendStockList(
                    sender,
                    BoxAPI.api().getStockManager().getPersonalStockHolder(target),
                    3 < args.length ? Arrays.copyOfRange(args, 3, args.length) : null
            );
        } else {
            ErrorMessages.PLAYER_NOT_FOUND.apply(args[2]).source(msgSrc).send(sender);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            return Collections.emptyList();
        }

        if (args.length == 3) {
            return TabCompleter.players(args[2]);
        }

        if (args.length == 4) {
            return SharedStockListCommand.getArgumentTypes();
        }

        int index = args.length - 1;
        return SharedStockListCommand.createTabCompletion(args[index - 1], args[index]);
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc).appendNewline().append(this.sharedStockListCommand.getArgHelp().create(msgSrc));
    }
}
