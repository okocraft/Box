package net.okocraft.box.feature.gui.internal.button;

import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.internal.menu.CategoryMenu;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CategoryButton implements Button {

    private final Category category;
    private final CategoryMenu menu;
    private final int slot;

    public CategoryButton(@NotNull Category category, int slot) {
        this.category = category;
        this.menu = new CategoryMenu(category);
        this.slot = slot;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        var icon = new ItemStack(category.getIconMaterial());

        icon.editMeta(meta -> meta.displayName(category.getDisplayName(session.getViewer()).style(Styles.NO_DECORATION_GOLD)));

        return icon;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        return ClickResult.changeMenu(menu);
    }
}
