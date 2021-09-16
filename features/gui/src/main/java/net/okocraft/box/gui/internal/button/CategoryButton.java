package net.okocraft.box.gui.internal.button;

import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.feature.category.model.Category;
import net.okocraft.box.gui.api.util.TranslationUtil;
import net.okocraft.box.gui.internal.lang.Styles;
import net.okocraft.box.gui.internal.menu.CategoryMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CategoryButton extends MenuButton {

    private final Category category;
    private final int slot;

    public CategoryButton(@NotNull Category category, int slot) {
        super(() -> new CategoryMenu(category));
        this.category = category;
        this.slot = slot;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return category.getIconMaterial();
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        var displayName =
                TranslationUtil.render(category.getDisplayName(), viewer)
                        .style(Styles.NO_STYLE)
                        .color(NamedTextColor.GOLD);

        target.displayName(displayName);

        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }
}
