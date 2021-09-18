package net.okocraft.box.feature.gui.internal.command;

import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.menu.CategorySelectorMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MenuOpenCommand extends AbstractCommand {

    public MenuOpenCommand() {
        super("gui", "box.command.gui", Set.of("g", "menu", "m"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }

        MenuOpener.open(new CategorySelectorMenu(), player);
    }
}
