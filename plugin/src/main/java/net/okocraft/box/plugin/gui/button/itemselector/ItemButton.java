package net.okocraft.box.plugin.gui.button.itemselector;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.gui.BoxInventoryHolder;
import net.okocraft.box.plugin.gui.OperationSelector;
import net.okocraft.box.plugin.gui.button.AbstractButton;
import net.okocraft.box.plugin.gui.button.ButtonIcon;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;

public class ItemButton extends AbstractButton {

    private final Item item;

    public ItemButton(@NotNull Item item) {
        super(new ButtonIcon(item.getOriginalCopy()));
        this.item = item;
        
        icon.applyConfig("item-element");
        //TODO: ぷれほる
        icon.applyPlaceHolder(null);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e) {
        User user = PLUGIN.getUserManager().getUser(e.getWhoClicked().getUniqueId());

        //FIXME: タイトルのハードコード
        BoxInventoryHolder invHolder = new OperationSelector(user, item, "操作", (BoxInventoryHolder) e.getInventory().getHolder());
        e.getWhoClicked().openInventory(invHolder.getInventory());
    }

    @Override
    public void update() {
        // TODO: 在庫などのプレホルについて、displayNameやloreを更新する。
    }
    
}
