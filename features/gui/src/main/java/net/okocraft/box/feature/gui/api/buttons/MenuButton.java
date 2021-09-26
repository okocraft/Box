package net.okocraft.box.feature.gui.api.buttons;

import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.util.MenuOpener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class MenuButton implements Button {

    private final Supplier<Menu> menuSupplier;

    protected MenuButton(@NotNull Supplier<Menu> menuSupplier) {
        this.menuSupplier = menuSupplier;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        MenuOpener.open(menuSupplier.get(), clicker);
    }
}
