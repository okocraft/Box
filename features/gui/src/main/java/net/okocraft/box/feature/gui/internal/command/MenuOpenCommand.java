package net.okocraft.box.feature.gui.internal.command;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.feature.gui.internal.menu.CategorySelectorMenu;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MenuOpenCommand extends AbstractCommand {

    public MenuOpenCommand() {
        super("gui", "box.command.gui", Set.of("g", "menu", "m"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return;
        }

        var menu = new CategorySelectorMenu();
        var holder = new BoxInventoryHolder(menu);

        holder.initializeMenu(player);
        holder.applyContents();

        CompletableFuture.runAsync(
                () -> player.openInventory(holder.getInventory()),
                BoxProvider.get().getExecutorProvider().getMainThread()
        ).join();
    }
}
