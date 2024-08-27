package net.okocraft.box.feature.gui.internal.button;

import net.kyori.adventure.text.format.TextDecoration.State;
import net.okocraft.box.feature.category.api.category.Category;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.internal.menu.CategoryMenu;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

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
        return this.slot;
    }

    @Override
    public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
        return ItemEditor.create()
            .displayName(this.category.getDisplayName(session.getViewer()).colorIfAbsent(GOLD).decorationIfAbsent(ITALIC, State.FALSE))
            .createItem(this.category.getIconMaterial());
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull ClickType clickType) {
        this.menu.setCurrentPage(session, 1);
        return ClickResult.changeMenu(this.menu);
    }
}
