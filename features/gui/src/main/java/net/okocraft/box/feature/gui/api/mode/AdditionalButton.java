package net.okocraft.box.feature.gui.api.mode;

import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class AdditionalButton implements Button {

    private final Supplier<Menu> menuSupplier;
    private int slot;

    protected AdditionalButton(@NotNull Supplier<Menu> menuSupplier) {
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
        MenuOpener.open(menuSupplier.get(), clicker);
    }
}
