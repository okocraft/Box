package net.okocraft.box.plugin.gui;

import java.util.stream.Collectors;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.gui.button.categoryselector.CategoryButton;

public class CategorySelector extends BoxInventoryHolder {

    public CategorySelector(@NotNull String title) {
        super(MenuType.CATEGORY_SELECTOR, title);

        putElementAndPageArrow(JavaPlugin.getPlugin(Box.class)
                .getCategoryManager().getCategories().stream()
                .map(CategoryButton::new).collect(Collectors.toList()));

        setItems();
    }
}
