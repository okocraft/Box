package net.okocraft.box.feature.gui.internal.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import net.okocraft.box.feature.gui.internal.menu.CategorySelectorMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class MenuOpenCommand extends AbstractCommand {

    public MenuOpenCommand() {
        super("gui", "box.command.gui", Set.of("g", "menu", "m"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }

        var session =PlayerSession.get(player);
        session.setBoxItemClickMode(ClickModeRegistry.getModes().get(0));
        session.resetCustomNumbers();

        MenuOpener.open(new CategorySelectorMenu(), player);
    }

    @Override
    public @NotNull Component getHelp() {
        return translatable("box.gui.command-help.command-line", AQUA)
                .append(text(" - ", DARK_GRAY))
                .append(translatable("box.gui.command-help.description", GRAY));
    }
}
