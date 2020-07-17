package net.okocraft.box.api.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Box に登録されているアイテムを取得したり、新たにアイテムを登録するためのインターフェース。
 */
public interface ItemManager {

    /**
     * 登録済みのアイテムを名前から取得する。
     *
     * @param name アイテム名
     * @return その名前のアイテムが登録されていたらその {@link BoxItem}, そうでなければ {@code null}
     */
    @Nullable BoxItem searchItem(@NotNull String name);

    /**
     * Box のアイテムデータベースに登録されているアイテムをすべて返す。
     *
     * @return すべての登録済みのアイテム
     */
    @NotNull Collection<BoxItem> getAllItems();

    /**
     * アイテムを Box のアイテムデータベースに登録する。
     *
     * @param item 登録するアイテム
     * @return 登録に成功したら {@link BoxItem}, すでに登録済みなどなんらかの理由で失敗した場合は {@code null}
     */
    @Nullable BoxItem register(@NotNull ItemStack item);

    /**
     * 渡したアイテムがすでに登録されているかどうか。
     *
     * @param item 判定するアイテム
     * @return 登録されていたら {@code true}, そうでなければ {@code false}
     */
    boolean isRegistered(@NotNull ItemStack item);

    /**
     * 渡した名前のアイテムがすでに登録されているかどうか。
     *
     * @param name 判定するアイテム名
     * @return 登録されていたら {@code true}, そうでなければ {@code false}
     */
    boolean isRegistered(@NotNull String name);
}