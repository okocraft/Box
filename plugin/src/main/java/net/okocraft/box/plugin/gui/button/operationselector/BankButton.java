package net.okocraft.box.plugin.gui.button.operationselector;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.gui.BoxInventoryHolder;
import net.okocraft.box.plugin.gui.button.ButtonIcon;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.sound.BoxSound;

public class BankButton extends AbstractOperationButton {

    public BankButton(@NotNull User user, @NotNull Item item) {
        super(new ButtonIcon(item.getOriginalCopy()), user, item);

        icon.applyConfig("bank-element");
        //TODO: ぷれほる
        icon.applyPlaceHolder(null);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e) {
        boolean isChanged = (e.isRightClick() ? withdraw() : deposit()) != 0;
        if (!isChanged) {
            SOUND_PLAYER.play((Player) e.getWhoClicked(), BoxSound.ITEM_NOT_ENOUGH);
            return;
        }

        BoxSound sound = e.isRightClick() ? BoxSound.ITEM_WITHDRAW : BoxSound.ITEM_DEPOSIT;
        SOUND_PLAYER.play((Player) e.getWhoClicked(), sound);

        update();
        ((BoxInventoryHolder) e.getInventory().getHolder()).setItem(e.getSlot());
    }

    /**
     * アイテムを預ける。サウンドは鳴らさない。
     * 
     * @return 預けられたアイテムの量
     */
    private int deposit() {
        Player player = Bukkit.getPlayer(user.getUuid());
        if (player == null) {
            return 0;
        }
        ItemStack taken = item.getOriginalCopy();
        taken.setAmount(quantity);
        int increment = quantity - player.getInventory().removeItem(taken).values().stream().mapToInt(ItemStack::getAmount).sum();
        if (increment != 0) {
            user.increase(item, increment);
        }
        return increment;
    }

    /**
     * アイテムを引き出す。サウンドは鳴らさない。
     * 
     * @return 引き出されたアイテムの量
     */
    private int withdraw() {
        Player player = Bukkit.getPlayer(user.getUuid());
        if (player == null) {
            return 0;
        }
        int amount = Math.min(quantity, user.getAmount(item));
        ItemStack given = item.getOriginalCopy();
        given.setAmount(amount);
        int decrement = amount - safeAddItem(player.getInventory(), given).values().stream().mapToInt(ItemStack::getAmount).sum();
        if (decrement != 0) {
            user.decrease(item, decrement);
        }
        return decrement;
    }

    @Override
    public void update() {
        //TODO: loreなどをストックに合わせて修正する。
        getIcon().setLore(List.of(""));
    }
}
