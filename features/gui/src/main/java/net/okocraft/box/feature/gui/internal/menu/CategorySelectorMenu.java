package net.okocraft.box.feature.gui.internal.menu;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.category.CategoryHolder;
import net.okocraft.box.feature.category.model.Category;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.menu.paginate.AbstractPaginatedMenu;
import net.okocraft.box.feature.gui.internal.button.CategoryButton;
import net.okocraft.box.feature.gui.internal.button.CloseButton;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class CategorySelectorMenu extends AbstractPaginatedMenu<Category> {

    public CategorySelectorMenu() {
        super(CategoryHolder.get());
    }

    @Override
    protected @NotNull Button createButton(@NotNull Category instance, int slot) {
        return new CategoryButton(instance, slot);
    }

    @Override
    protected void addAdditionalButtons(@NotNull Player viewer, @NotNull List<Button> buttons) {
        buttons.add(new CloseButton());

        Stream.of(45, 46, 47, 48, 50, 51, 52, 53)
                .map(slot -> Button.empty(Material.GRAY_STAINED_GLASS_PANE, slot))
                .forEach(buttons::add);
    }

    @Override
    public int getRows() {
        return 6;
    }

    @Override
    public @NotNull Component getTitle() {
        return Displays.CATEGORY_SELECTOR_MENU;
    }

    @Override
    public void onOpen(@NotNull Player viewer) {
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_CHEST_OPEN, 100f, 1.5f);
    }
}
