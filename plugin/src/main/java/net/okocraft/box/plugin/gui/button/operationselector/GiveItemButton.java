package net.okocraft.box.plugin.gui.button.operationselector;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.gui.BoxInventoryHolder;
import net.okocraft.box.plugin.gui.UserSelector;
import net.okocraft.box.plugin.gui.button.ButtonIcon;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.sound.BoxSound;

/**
 * 右クリックでプレイヤー選択、左クリックでアイテムの譲渡を行うボタン。
 */
public class GiveItemButton extends AbstractOperationButton {

    private User receiver;

    public GiveItemButton(@NotNull User user, @NotNull Item item) {
        super(new ButtonIcon(new ItemStack(Material.PLAYER_HEAD)), user, item);

        icon.applyConfig("give-item-element");
        // TODO: ぷれほる
        icon.applyPlaceHolder(null);
    }

    private void setReceiver(User receiver) {
        this.receiver = receiver;
        getIcon().setHeadOwner(Bukkit.getOfflinePlayer(receiver.getUuid()));
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e) {
        if (receiver == null || e.isRightClick()) {
            //FIXME: タイトルハードコード
            UserSelector userSelector = new UserSelector("プレイヤーを選んでください。", selected -> {
                setReceiver(selected);
                e.getWhoClicked().openInventory(e.getInventory());
            });

            e.getWhoClicked().openInventory(userSelector.getInventory());
            SOUND_PLAYER.play((Player) e.getWhoClicked(), BoxSound.MENU_OPEN);
            return;
        }

        if (give() != 0) {
            SOUND_PLAYER.play((Player) e.getWhoClicked(), BoxSound.ITEM_NOT_ENOUGH);
            return;
        }

        //TODO: 新しい音
        SOUND_PLAYER.play((Player) e.getWhoClicked(), BoxSound.ITEM_DEPOSIT);
        update();
        ((BoxInventoryHolder) e.getInventory().getHolder()).setItem(e.getSlot());
    }

    /**
     * アイテムを預ける。サウンドは鳴らさない。
     * 
     * @return 預けられたアイテムの量
     */
    private int give() {
        Player player = Bukkit.getPlayer(user.getUuid());
        if (player == null || receiver == null) {
            return 0;
        }
        ItemStack taken = item.getOriginalCopy();
        taken.setAmount(quantity);
        int increment = quantity - player.getInventory().removeItem(taken).values().stream().mapToInt(ItemStack::getAmount).sum();
        if (increment != 0) {
            receiver.increase(item, increment);
        }
        return increment;
    }

    @Override
    public void update() {
        //TODO: loreなどをストックに合わせて修正する。
        getIcon().setLore(List.of(""));
    }
}
