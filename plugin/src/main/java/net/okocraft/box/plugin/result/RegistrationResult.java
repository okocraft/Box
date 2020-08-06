package net.okocraft.box.plugin.result;

import net.okocraft.box.api.util.Result;

/**
 * Box のデータベースへのアイテム登録を試行した結果を列挙するクラス。
 */
public enum RegistrationResult implements Result {

    /**
     * 正常に登録完了
     */
    SUCCESS,

    /**
     * すでに登録済み
     */
    ALREADY_REGISTERED,

    /**
     * 例外が発生
     */
    EXCEPTION_OCCURS;

    /**
     * 結果が成功したことを示しているか。
     *
     * @return {@link RegistrationResult#SUCCESS} なら {@code true}, そうでなければ {@code false}
     */
    @Override
    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
