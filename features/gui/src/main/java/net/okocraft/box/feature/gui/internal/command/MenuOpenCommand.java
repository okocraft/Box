package net.okocraft.box.feature.gui.internal.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.Components;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.listener.ItemInfoEventListener;
import net.okocraft.box.feature.gui.api.event.MenuOpenEvent;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.menu.paginate.PaginatedMenu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.menu.CategoryMenu;
import net.okocraft.box.feature.gui.internal.menu.CategorySelectorMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;

public class MenuOpenCommand extends AbstractCommand {

    private static final String OTHER_PLAYERS_GUI_PERMISSION = "box.admin.command.gui.other";
    private static final Component CANNOT_OPEN_MENU = Components.redTranslatable("box.gui.cannot-open-menu");
    private static final Component COMMAND_HELP = Components.commandHelp("box.gui.command-help", false);
    private static final SingleArgument<String> CATEGORY_NOT_FOUND = arg -> Components.redTranslatable("box.gui.category-not-found", Components.aquaText(arg));

    public MenuOpenCommand() {
        super("gui", "box.command.gui", Set.of("g", "menu", "m"));

        ItemInfoEventListener.setCommandCreator(((category, item) -> {
            var name = CategoryRegistry.get().getRegisteredName(category);
            int page = category.getItems().indexOf(item) / 45 + 1;
            return "/box gui --category " + name + " --page " + page;
        }));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        if (args.length < 2) {
            openMenu(PlayerSession.newSession(player), new CategorySelectorMenu());
            return;
        }

        if (!args[1].startsWith("-")) {
            legacyBehavior(player, args);
            return;
        }

        PlayerSession session = null;
        Menu menu = null;
        int page = 0;

        for (int i = 1; i + 1 < args.length; i = i + 2) {
            var arg = args[i].toLowerCase(Locale.ENGLISH);

            if (session == null && (arg.equalsIgnoreCase("-p") || arg.equalsIgnoreCase("--player"))) {
                if (!player.hasPermission(OTHER_PLAYERS_GUI_PERMISSION)) {
                    player.sendMessage(GeneralMessage.ERROR_NO_PERMISSION.apply(OTHER_PLAYERS_GUI_PERMISSION));
                    return;
                }

                var onlinePlayer = Bukkit.getPlayer(args[i + 1]);
                var playerMap = BoxProvider.get().getBoxPlayerMap();

                if (onlinePlayer != null && playerMap.isLoaded(onlinePlayer)) {
                    session = PlayerSession.newSession(player, playerMap.get(onlinePlayer));
                } else {
                    var offlineUser = UserSearcher.search(args[i + 1]);

                    if (offlineUser != null) {
                        session = PlayerSession.newSession(player);
                        session.setStockHolder(BoxProvider.get().getStockManager().getPersonalStockHolder(offlineUser));
                    } else {
                        player.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[i + 1]));
                        return;
                    }
                }
            } else if (menu == null && (arg.equalsIgnoreCase("-c") || arg.equalsIgnoreCase("--category"))) {
                var category = CategoryRegistry.get().getByName(args[i + 1]);

                if (category.isEmpty()) {
                    player.sendMessage(CATEGORY_NOT_FOUND.apply(args[i + 1]));
                    return;
                }

                menu = new CategoryMenu(category.get());
            } else if (arg.equalsIgnoreCase("-pa") || arg.equalsIgnoreCase("--page")) {
                try {
                    page = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ignored) {
                    player.sendMessage(GeneralMessage.ERROR_COMMAND_INVALID_NUMBER.apply(args[i + 1]));
                    return;
                }
            }
        }

        if (session == null) {
            session = PlayerSession.newSession(player);
        }

        if (menu == null) {
            menu = new CategorySelectorMenu();
        } else {
            session.rememberMenu(new CategorySelectorMenu()); // for back button
        }

        //noinspection ConstantValue
        if (1 <= page && menu instanceof PaginatedMenu paginatedMenu) {
            session.putData(PaginatedMenu.CURRENT_PAGE_KEY, Math.min(page, paginatedMenu.getMaxPage()));
        }

        openMenu(session, menu);
    }

    @Override
    public @NotNull Component getHelp() {
        return COMMAND_HELP;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (2 < args.length) {
            var arg = args[args.length - 2].toLowerCase(Locale.ENGLISH);

            if (arg.equalsIgnoreCase("-p") || arg.equalsIgnoreCase("--player")) {
                return sender.hasPermission(OTHER_PLAYERS_GUI_PERMISSION) ?
                        TabCompleter.players(args[args.length - 1]) :
                        Collections.emptyList();
            }

            if (arg.equalsIgnoreCase("-c") || arg.equalsIgnoreCase("--category")) {
                return CategoryRegistry.get().names()
                        .stream()
                        .filter(name -> name.startsWith(args[args.length - 1]))
                        .toList();
            }

            if (arg.equalsIgnoreCase("-pa") || arg.equalsIgnoreCase("--page")) {
                return IntStream.rangeClosed(1, 10).mapToObj(Integer::toString).toList();
            }
        }

        return sender.hasPermission(OTHER_PLAYERS_GUI_PERMISSION) ?
                List.of("--player", "--category", "--page", "-p", "-c", "-pa") :
                List.of("--category", "-c", "--page", "-pa");
    }

    private void legacyBehavior(@NotNull Player player, @NotNull String[] args) {
        if (!player.hasPermission(OTHER_PLAYERS_GUI_PERMISSION)) {
            player.sendMessage(GeneralMessage.ERROR_NO_PERMISSION.apply(OTHER_PLAYERS_GUI_PERMISSION));
            return;
        }

        var user = UserSearcher.search(args[1]);

        if (user == null) {
            player.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[1]));
            return;
        }

        var session = PlayerSession.newSession(player);
        session.setStockHolder(BoxProvider.get().getStockManager().getPersonalStockHolder(user));

        openMenu(session, new CategorySelectorMenu());
    }

    private void openMenu(@NotNull PlayerSession session, @NotNull Menu menu) {
        BoxProvider.get().getEventManager().callAsync(new MenuOpenEvent(menu, session), event -> {
            if (event.isCancelled()) {
                session.getViewer().sendMessage(CANNOT_OPEN_MENU);
            } else {
                MenuOpener.open(menu, session);
            }
        });
    }
}
