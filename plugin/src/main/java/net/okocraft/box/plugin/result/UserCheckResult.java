package net.okocraft.box.plugin.result;

import net.okocraft.box.api.util.Result;

/**
 * ユーザーをチェックした結果を列挙するクラス。
 */
public enum UserCheckResult implements Result {

    /**
     * 変更なし
     */
    NONE,

    /**
     * 新しいプレイヤー
     */
    NEW_PLAYER,

    /**
     * 改名後初ログイン
     */
    RENAMED,

    /**
     * 例外が発生
     */
    EXCEPTION_OCCURS;

    /**
     * 結果が成功したことを示しているか。
     *
     * @return {@link UserCheckResult#EXCEPTION_OCCURS} なら {@code false}, そうでなければ {@code true}
     */
    @Override
    public boolean isSuccess() {
        return this != EXCEPTION_OCCURS;
    }
}
