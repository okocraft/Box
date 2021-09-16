package net.okocraft.box.feature.gui.internal.button;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.internal.holder.BoxInventoryHolder;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class MenuButton implements Button {

    private final Supplier<Menu> menuSupplier;

    protected MenuButton(@NotNull Supplier<Menu> menuSupplier) {
        this.menuSupplier = menuSupplier;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        var holder = new BoxInventoryHolder(menuSupplier.get());

        holder.initializeMenu(clicker);
        holder.applyContents();

        CompletableFuture.runAsync(
                () -> clicker.openInventory(holder.getInventory()),
                BoxProvider.get().getExecutorProvider().getMainThread()
        ).join();

        clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 2.0f);
    }
}
