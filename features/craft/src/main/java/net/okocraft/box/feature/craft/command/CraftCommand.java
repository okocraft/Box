package net.okocraft.box.feature.craft.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.feature.craft.RecipeRegistry;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.menu.CraftMenu;
import net.okocraft.box.feature.craft.menu.RecipeSelector;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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

        var item = BoxProvider.get().getItemManager().getBoxItem(args[1]);

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

        if (recipeHolder.getRecipeList().size() == 1) {
            menu = new CraftMenu(recipeHolder.getRecipeList().get(0), null);
        } else {
            menu = new RecipeSelector(item.get(), recipeHolder, null);
        }

        MenuOpener.open(menu, player);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player) || args.length != 2) {
            return Collections.emptyList();
        }

        var itemNameFilter = args[1].toUpperCase(Locale.ROOT);
        return BoxProvider.get()
                .getItemManager()
                .getItemNameSet()
                .stream()
                .filter(itemName -> itemName.startsWith(itemNameFilter))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull Component getHelp() {
        return Displays.COMMAND_HELP;
    }
}
