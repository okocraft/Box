package net.okocraft.box.plugin.gui.button;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.gui.BoxInventoryHolder;
import net.okocraft.box.plugin.sound.BoxSound;

public class NextPageButton extends AbstractButton {

    private int guiPage = 1;

    public NextPageButton() {
        super(
            new ButtonIcon(new ItemStack(Material.ARROW))
                    .setDisplayName(ChatColor.GOLD + "次のページ")
                    .setLore(List.of())
                    .setGlowing(false) // default value
        );

        icon.applyConfig("next-page");
        //TODO: ぷれほる
        icon.applyPlaceHolder(null);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e) {
        BoxInventoryHolder holder = (BoxInventoryHolder) e.getInventory().getHolder();
        guiPage++;
        holder.setPage(guiPage);
        guiPage = holder.getPage();
        PLUGIN.getSoundPlayer().play((Player) e.getWhoClicked(), BoxSound.MENU_PAGE_CHANGE);
        update();
    }

    @Override
    public void update() {
        // TODO: set lore or name depending on gui page.
    }
    
}
