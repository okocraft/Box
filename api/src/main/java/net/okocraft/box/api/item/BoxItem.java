package net.okocraft.box.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Box 内でのアイテム管理に使用するインターフェース
 */
public interface BoxItem {

    /**
     * 内部の一意な ID を取得する。
     *
     * @return 内部の一意な ID
     */
    int getInternalID();

    /**
     * アイテムの名前を取得する。
     *
     * @return アイテムの名前
     */
    @NotNull String getItemName();

    /**
     * アイテムの名前(設定可能)を取得する。
     *
     * @return アイテムの設定可能な名前
     */
    @NotNull String getCustomName();

    /**
     * {@link ItemStack} として取得する。
     *
     * @return {@link ItemStack}
     */
    @NotNull ItemStack toItemStack();

    /**
     * このアイテムの売却価格を取得する。
     *
     * @return 売却価格, 未設定であれば {@code 0}
     */
    double getSellingPrice();

    /**
     * このアイテムの購入価格を取得する。
     *
     * @return 購入価格, 未設定であれば {@code 0}
     */
    double getBuyingPrice();
}
