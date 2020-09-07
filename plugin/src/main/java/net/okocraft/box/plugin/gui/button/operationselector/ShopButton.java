package net.okocraft.box.plugin.gui.button.operationselector;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import net.milkbowl.vault.economy.Economy;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.config.PriceConfig;
import net.okocraft.box.plugin.gui.BoxInventoryHolder;
import net.okocraft.box.plugin.gui.button.ButtonIcon;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.sound.BoxSound;

public class ShopButton extends AbstractOperationButton {

    private static final PriceConfig PRICE_CONFIG = PLUGIN.getPriceConfig();
    //TODO: vaultホック
    private final Economy economy = PLUGIN.getEconomy();
    
    public ShopButton(@NotNull User user, @NotNull Item item) {
        super(new ButtonIcon(item.getOriginalCopy()), user, item);

        icon.applyConfig("shop-element");
        //TODO: ぷれほる
        icon.applyPlaceHolder(null);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent e) {
        boolean isChanged = (e.isRightClick() ? sell() : buy()) != 0;
        if (!isChanged) {
            SOUND_PLAYER.play((Player) e.getWhoClicked(), BoxSound.ITEM_NOT_ENOUGH);
            return;
        }

        BoxSound sound = e.isRightClick() ? BoxSound.ITEM_SELL : BoxSound.ITEM_BUY;
        SOUND_PLAYER.play((Player) e.getWhoClicked(), sound);

        update();
        ((BoxInventoryHolder) e.getInventory().getHolder()).setItem(e.getSlot());
    }

    /**
     * アイテムを買う。サウンドは鳴らさない。
     * 
     * @return 購入したアイテムの量
     */
    private int buy() {
        double price = PRICE_CONFIG.getBuyingPrice(item);
        if (price == 0) {
            return 0;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(user.getUuid());
        int quantity = Math.min(this.quantity, (int) (economy.getBalance(player) / price));
        if (quantity > 0) {
            economy.withdrawPlayer(player, quantity * price);
            user.increase(item, quantity);
        }
        return quantity;
    }

    /**
     * アイテムを売る。サウンドは鳴らさない。
     * 
     * @return 売却したアイテムの量
     */
    private int sell() {
        double price = PRICE_CONFIG.getSellingPrice(item);
        if (price == 0) {
            return 0;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(user.getUuid());
        int quantity = Math.min(this.quantity, user.getAmount(item));
        if (quantity > 0) {
            economy.depositPlayer(player, quantity * price);
            user.decrease(item, quantity);
        }
        return quantity;
    }

    @Override
    public void update() {
        //TODO: loreなどをストックに合わせて修正する。
        getIcon().setLore(List.of(""));
    }
}
