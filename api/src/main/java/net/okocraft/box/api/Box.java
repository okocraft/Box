package net.okocraft.box.api;

import net.okocraft.box.api.item.ItemManager;
import net.okocraft.box.api.user.UserManager;
import org.jetbrains.annotations.NotNull;

/**
 * Box API クラス。
 */
public interface Box {

    /**
     * ユーザー管理用のインスタンスを取得する。
     *
     * @return {@link UserManager}
     */
    @NotNull UserManager getUserManager();

    /**
     * アイテム管理用のインスタンスを取得する。
     *
     * @return {@link ItemManager}
     */
    @NotNull ItemManager getItemManager();
}
