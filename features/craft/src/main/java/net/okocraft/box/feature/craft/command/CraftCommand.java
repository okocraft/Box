package net.okocraft.box.feature.craft.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.Components;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.util.TabCompleter;
import net.okocraft.box.feature.craft.RecipeRegistry;
import net.okocraft.box.feature.craft.lang.Displays;
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

    public CraftCommand() {
        super("craft", "box.command.craft", Set.of("c"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            sender.sendMessage(getHelp());
            return;
        }

        var item = BoxAPI.api().getItemManager().getBoxItem(args[1]);

        if (item.isEmpty()) {
            player.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[1]));
            return;
        }

        var recipeHolder = RecipeRegistry.getRecipes(item.get());

        if (recipeHolder == null || recipeHolder.getRecipeList().isEmpty()) {
            player.sendMessage(Displays.COMMAND_RECIPE_NOT_FOUND.apply(item.get()));
            return;
        }

        Menu menu;
        var session = PlayerSession.newSession(player);

        if (recipeHolder.getRecipeList().size() == 1) {
            menu = CraftMenu.prepare(session, recipeHolder.getRecipeList().get(0));
        } else {
            menu = new RecipeSelectorMenu(item.get(), recipeHolder);
        }

        BoxAPI.api().getEventManager().callAsync(new MenuOpenEvent(menu, session), event -> {
            if (event.isCancelled()) {
                session.getViewer().sendMessage(Components.redTranslatable("box.gui.cannot-open-menu"));
            } else {
                MenuOpener.open(menu, session);
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
    public @NotNull Component getHelp() {
        return Displays.COMMAND_HELP;
    }
}
