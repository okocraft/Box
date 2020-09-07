package net.okocraft.box.plugin.gui;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.gui.button.operationselector.BankButton;
import net.okocraft.box.plugin.gui.button.operationselector.CraftButton;
import net.okocraft.box.plugin.gui.button.operationselector.GiveItemButton;
import net.okocraft.box.plugin.gui.button.operationselector.ShopButton;
import net.okocraft.box.plugin.gui.button.BackMenuButton;
import net.okocraft.box.plugin.gui.button.Button;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;

public class OperationSelector extends BoxInventoryHolder {

    public OperationSelector(@NotNull User user, @NotNull Item item, @NotNull String title, @NotNull BoxInventoryHolder previousMenu) {
        super(MenuType.OPERATION_SELECTOR, title);

        Map<Integer, Button> buttons = buttonList.getPageButtons(1);
        buttons.put(0, new BankButton(user, item));
        buttons.put(2, new ShopButton(user, item));
        buttons.put(4, new CraftButton(user, item));
        buttons.put(6, new GiveItemButton(user, item));
        buttons.put(8, new BackMenuButton(previousMenu));
    }
    
}
