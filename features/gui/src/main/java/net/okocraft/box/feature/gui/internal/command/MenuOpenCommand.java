package net.okocraft.box.feature.gui.internal.command;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.ComponentLike;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.api.util.UserSearcher;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.listener.ItemInfoEventListener;
import net.okocraft.box.feature.gui.api.event.MenuOpenEvent;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.menu.paginate.PaginatedMenu;
import net.okocraft.box.feature.gui.api.session.MenuHistoryHolder;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.menu.CategoryMenu;
import net.okocraft.box.feature.gui.internal.menu.CategorySelectorMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

public class MenuOpenCommand extends AbstractCommand {

    private static final String OTHER_PLAYERS_GUI_PERMISSION = "box.admin.command.gui.other";

    private final MessageKey help;
    private final MessageKey cannotOpen;
    private final MessageKey.Arg1<String> categoryNotFound;

    public MenuOpenCommand(@NotNull DefaultMessageCollector collector) {
        super("gui", "box.command.gui", Set.of("g", "menu", "m"));

        this.help = MessageKey.key(collector.add("box.gui.command.help", "<aqua>/box gui<dark_gray> - <gray>Opens Box menu"));
        this.cannotOpen = MessageKey.key(collector.add("box.gui.command.cannot-open-menu", "<red>Cannot open the menu."));
        this.categoryNotFound = MessageKey.arg1(collector.add("box.gui.command.category-not-found", "<red>Category <aqua><arg><red> not found"), Placeholders.ARG);

        ItemInfoEventListener.setCommandCreator(((category, item) -> {
            String name = CategoryRegistry.get().getRegisteredName(category);
            int page = category.getItems().indexOf(item) / 45 + 1;
            return "/box gui --category " + name + " --page " + page;
        }));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ErrorMessages.COMMAND_ONLY_PLAYER);
            return;
        }

        if (args.length < 2) {
            this.openMenu(PlayerSession.newSession(player), new CategorySelectorMenu());
            return;
        }

        if (!args[1].startsWith("-")) {
            this.legacyBehavior(player, args);
            return;
        }

        PlayerSession session = null;
        Menu menu = null;
        int page = 0;

        for (int i = 1; i + 1 < args.length; i = i + 2) {
            String arg = args[i].toLowerCase(Locale.ENGLISH);

            if (session == null && (arg.equalsIgnoreCase("-p") || arg.equalsIgnoreCase("--player"))) {
                if (!player.hasPermission(OTHER_PLAYERS_GUI_PERMISSION)) {
                    sender.sendMessage(ErrorMessages.NO_PERMISSION.apply(OTHER_PLAYERS_GUI_PERMISSION));
                    return;
                }

                BoxUser targetUser = UserSearcher.search(args[i + 1]);

                if (targetUser != null) {
                    session = PlayerSession.newSession(player, targetUser);
                } else {
                    sender.sendMessage(ErrorMessages.PLAYER_NOT_FOUND.apply(args[i + 1]));
                    return;
                }
            } else if (menu == null && (arg.equalsIgnoreCase("-c") || arg.equalsIgnoreCase("--category"))) {
                Optional<Category> category = CategoryRegistry.get().getByName(args[i + 1]);

                if (category.isEmpty()) {
                    sender.sendMessage(this.categoryNotFound.apply(args[i + 1]));
                    return;
                }

                menu = new CategoryMenu(category.get());
            } else if (arg.equalsIgnoreCase("-pa") || arg.equalsIgnoreCase("--page")) {
                try {
                    page = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ignored) {
                    sender.sendMessage(ErrorMessages.INVALID_NUMBER.apply(args[i + 1]));
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
            MenuHistoryHolder.getFromSession(session).rememberMenu(new CategorySelectorMenu()); // for back button
        }

        //noinspection ConstantValue
        if (1 <= page && menu instanceof PaginatedMenu paginatedMenu) {
            paginatedMenu.setCurrentPage(session, Math.min(page, paginatedMenu.getMaxPage()));
        }

        this.openMenu(session, menu);
    }

    @Override
    public @NotNull ComponentLike getHelp() {
        return this.help;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2 || !(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (2 < args.length) {
            String arg = args[args.length - 2].toLowerCase(Locale.ENGLISH);

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
            player.sendMessage(ErrorMessages.NO_PERMISSION.apply(OTHER_PLAYERS_GUI_PERMISSION));
            return;
        }

        BoxUser user = UserSearcher.search(args[1]);

        if (user == null) {
            player.sendMessage(ErrorMessages.PLAYER_NOT_FOUND.apply(args[1]));
            return;
        }

        this.openMenu(PlayerSession.newSession(player, user), new CategorySelectorMenu());
    }

    private void openMenu(@NotNull PlayerSession session, @NotNull Menu menu) {
        BoxAPI.api().getEventCallers().async().call(new MenuOpenEvent(menu, session), event -> {
            if (event.isCancelled()) {
                event.getViewer().sendMessage(this.cannotOpen);
            } else {
                MenuOpener.open(event.getMenu(), event.getSession());
            }
        });
    }
}
