package net.okocraft.box.plugin.gui.button.categoryselector;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.category.Category;
import net.okocraft.box.plugin.gui.BoxInventoryHolder;
import net.okocraft.box.plugin.gui.ItemSelector;
import net.okocraft.box.plugin.gui.button.AbstractButton;
import net.okocraft.box.plugin.gui.button.ButtonIcon;

public class CategoryButton extends AbstractButton {

    private final Category category;

    public CategoryButton(@NotNull Category category) {
        super(new ButtonIcon(category.getIcon()));
        this.category = category;

        icon.applyConfig("category");
        //TODO: ぷれほる
        icon.applyPlaceHolder(null);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e) {
        //FIXME: タイトルのハードコード
        BoxInventoryHolder invHolder = new ItemSelector(category, "操作");
        e.getWhoClicked().openInventory(invHolder.getInventory());
    }

    @Override
    public void update() {
    }
}
