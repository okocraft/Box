package net.okocraft.box.feature.gui.api.mode;

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

@SuppressWarnings("DuplicatedCode")
public abstract class SettingMenuButton implements Button {

    private final Supplier<Menu> menuSupplier;
    private int slot;

    protected SettingMenuButton(@NotNull Supplier<Menu> menuSupplier) {
        this.menuSupplier = menuSupplier;
    }

    @Override
    public final int getSlot() {
        return slot;
    }

    public final void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public final void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
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
