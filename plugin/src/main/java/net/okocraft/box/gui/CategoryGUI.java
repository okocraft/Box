package net.okocraft.box.gui;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

abstract class CategoryGUI extends QuantityGUI {

    private final String categoryName;
    private final NamespacedKey realItemKey = new NamespacedKey(plugin, "realitem");

    CategoryGUI(Player player, String categoryName, String guiTitle, int quantity) {
        super(player, guiTitle, 54, quantity, 45, 53, 49, 46, 47, 48);

        this.categoryName = categoryName;
    }

    String getCategoryName() {
        return categoryName;
    }

    /**
     * アイテムの取引などで変動したメタを追随させるためのメソッド。
     *
     * @param item メタを更新するアイテム
     */
    protected abstract void update(ItemStack item);

    /**
     * 全てのアイテムのloreを更新する。
     */
    protected void update() {
        for (int i = 0; i < 45; i++) {
            ItemStack item = getInventory().getItem(i);
            if (item != null) {
                update(item);
            }
        }
    }

    @Override
    void setPage(int page) {
        super.setPage(page);
        update();
    }

    String getRealItemName(ItemStack guiItem) {
        return guiItem.getItemMeta().getPersistentDataContainer().get(realItemKey, PersistentDataType.STRING);
    }

    ItemStack getRealItem(ItemStack guiItem) {
        String realItemName = getRealItemName(guiItem);
        if (realItemName == null) {
            return null;
        }
        return itemData.getItemStack(realItemName);
    }
}