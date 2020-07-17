package net.okocraft.box.api.user;

import net.okocraft.box.api.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * アイテムデータを保持するインターフェース
 */
public interface BoxDataHolder {

    /**
     * 指定したアイテムの所持数を取得する。
     *
     * @param item 取得するアイテム
     * @return アイテムの所持数
     */
    long getAmount(@NotNull BoxItem item);

    /**
     * 指定したアイテムの所持数をセットする。
     *
     * @param item   設定するアイテム
     * @param amount 設定する所持数
     */
    void setAmount(@NotNull BoxItem item, long amount);

    /**
     * 指定したアイテムの所持数を 1 増やす.
     *
     * @param item 増やすアイテム
     * @return 増加後の所持数
     */
    default long increase(@NotNull BoxItem item) {
        return increase(item, 1);
    }

    /**
     * 指定したアイテムの所持数を増やす。
     *
     * @param item   増やすアイテム
     * @param amount 増加量
     * @return 増加後の所持数
     */
    long increase(@NotNull BoxItem item, long amount);


    /**
     * 指定したアイテムの所持数を 1 減らす。
     *
     * @param item 減らすアイテム
     * @return 減少後の所持数
     */
    default long decrease(@NotNull BoxItem item) {
        return decrease(item, 1);
    }

    /**
     * 指定したアイテムの所持数を減らす
     *
     * @param item   減らすアイテム
     * @param amount 減少量
     * @return 減少後の所持数
     */
    long decrease(@NotNull BoxItem item, long amount);

    /**
     * 指定したアイテムを1つ以上持っているか。
     *
     * @param item 持っているか判定するアイテム
     * @return 持っていれば {@code true}, そうでなければ {@code false}
     */
    default boolean hasItem(@NotNull BoxItem item) {
        return hasItem(item, 1);
    }

    /**
     * 指定したアイテムを要求量以上持っているか。
     *
     * @param item    持っているか判定するアイテム。
     * @param require 要求量
     * @return 持っていれば {@code true}, そうでなければ {@code false}
     */
    default boolean hasItem(@NotNull BoxItem item, long require) {
        return require <= getAmount(item);
    }

    /**
     * 指定したアイテムが自動収納されるか。
     *
     * @param item 判定するアイテム
     * @return 自動収納されるなら {@code true}, そうでなければ {@code false}
     */
    boolean isAutoStore(@NotNull BoxItem item);
}
