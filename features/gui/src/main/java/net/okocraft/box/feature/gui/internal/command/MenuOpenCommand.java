package net.okocraft.box.feature.gui.internal.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserStockHolderOperator;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.menu.CategorySelectorMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class MenuOpenCommand extends AbstractCommand {

    private static final String OTHER_PLAYERS_GUI_PERMISSION = "box.admin.command.gui.other";

    public MenuOpenCommand() {
        super("gui", "box.command.gui", Set.of("g", "menu", "m"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        var session = PlayerSession.get(player);

        session.setBoxItemClickMode(session.getAvailableClickModes().get(0));
        session.resetCustomNumbers();

        StockHolder stockHolder;

        if (1 < args.length) {
            if (!sender.hasPermission(OTHER_PLAYERS_GUI_PERMISSION)) {
                sender.sendMessage(GeneralMessage.ERROR_NO_PERMISSION.apply(OTHER_PLAYERS_GUI_PERMISSION));
                return;
            }

            stockHolder = UserStockHolderOperator.create(args[1]).supportOffline(true).getUserStockHolder();

            if (stockHolder == null) {
                sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[1]));
                return;
            }
        } else {
            stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();
        }

        session.setStockHolder(stockHolder);

        MenuOpener.open(new CategorySelectorMenu(), player);
    }

    @Override
    public @NotNull Component getHelp() {
        return translatable("box.gui.command-help.command-line", AQUA)
                .append(text(" - ", DARK_GRAY))
                .append(translatable("box.gui.command-help.description", GRAY));
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2 && sender.hasPermission(OTHER_PLAYERS_GUI_PERMISSION)) {
            return TabCompleter.players(args[1]);
        } else {
            return Collections.emptyList();
        }
    }
}
