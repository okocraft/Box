package net.okocraft.box.gui.internal.button;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.gui.api.button.Button;
import net.okocraft.box.gui.api.menu.Menu;
import net.okocraft.box.gui.api.util.TranslationUtil;
import net.okocraft.box.gui.internal.holder.BoxInventoryHolder;
import net.okocraft.box.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BackButton implements Button {

    private final Menu menu;
    private final int slot;

    public BackButton(@NotNull Menu menu, int slot) {
        this.menu = menu;
        this.slot = slot;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.OAK_DOOR;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(Displays.BACK_BUTTON, viewer));
        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        var holder = new BoxInventoryHolder(menu);

        holder.initializeMenu(clicker);
        holder.applyContents();

        CompletableFuture.runAsync(
                () -> clicker.openInventory(holder.getInventory()),
                BoxProvider.get().getExecutorProvider().getMainThread()
        ).join();
    }
}
