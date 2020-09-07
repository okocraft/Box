package net.okocraft.box.plugin.gui;

import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.category.Category;
import net.okocraft.box.plugin.gui.button.itemselector.ItemButton;

public class ItemSelector extends BoxInventoryHolder {

    public ItemSelector(@NotNull Category category, @NotNull String title) {
        super(MenuType.ITEM_SELECTOR, title);

        putElementAndPageArrow(category.getItems().stream().map(ItemButton::new).collect(Collectors.toList()));
        setItems();
    }
    
}
