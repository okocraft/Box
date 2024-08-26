package net.okocraft.box.feature.craft.command;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.feature.craft.RecipeRegistry;
import net.okocraft.box.feature.craft.gui.menu.CraftMenu;
import net.okocraft.box.feature.craft.gui.menu.RecipeSelectorMenu;
import net.okocraft.box.feature.gui.api.event.MenuOpenEvent;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CraftCommand extends AbstractCommand {

    private final MiniMessageBase help;
    private final Arg1<BoxItem> recipeNotFound;
    private final MiniMessageBase cannotOpenMenu;

    public CraftCommand(@NotNull DefaultMessageCollector collector) {
        super("craft", "box.command.craft", Set.of("c"));
        this.help = MiniMessageBase.messageKey(collector.add("box.craft.command.help", "<aqua>/box craft <item name><dark_gray> - <gray>Shows the item recipe"));
        this.recipeNotFound = Arg1.arg1(collector.add("box.craft.command.craft.recipe-not-found", "<red>No recipes found for item <aqua><item>"), Placeholders.ITEM);
        this.cannotOpenMenu = MiniMessageBase.messageKey(collector.add("box.craft.command.craft.cannot-open-menu", "<red>Cannot open the recipe menu."));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (!(sender instanceof Player player)) {
            ErrorMessages.COMMAND_ONLY_PLAYER.source(msgSrc).send(sender);
            return;
        }

        if (args.length < 2) {
            ErrorMessages.NOT_ENOUGH_ARGUMENT.source(msgSrc).send(sender);
            sender.sendMessage(this.getHelp(msgSrc));
            return;
        }

        var item = BoxAPI.api().getItemManager().getBoxItem(args[1]);

        if (item.isEmpty()) {
            ErrorMessages.ITEM_NOT_FOUND.apply(args[1]).source(msgSrc).send(sender);
            return;
        }

        var recipeHolder = RecipeRegistry.getRecipes(item.get());

        if (recipeHolder == null || recipeHolder.getRecipeList().isEmpty()) {
            this.recipeNotFound.apply(item.get()).source(msgSrc).send(sender);
            return;
        }

        Menu menu;
        var session = PlayerSession.newSession(player);

        if (recipeHolder.getRecipeList().size() == 1) {
            menu = CraftMenu.prepare(recipeHolder.getRecipeList().getFirst());
        } else {
            menu = new RecipeSelectorMenu(item.get(), recipeHolder);
        }

        BoxAPI.api().getEventCallers().async().call(new MenuOpenEvent(menu, session), event -> {
            if (event.isCancelled()) {
                this.cannotOpenMenu.source(event.getSession().getMessageSource()).send(event.getViewer());
            } else {
                MenuOpener.open(event.getMenu(), event.getSession());
            }
        });
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player && args.length == 2) {
            return TabCompleter.itemNames(args[1]);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }
}
