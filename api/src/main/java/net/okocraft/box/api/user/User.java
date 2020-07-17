package net.okocraft.box.api.user;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * ユーザーインターフェース。
 */
public interface User extends BoxDataHolder {

    /**
     * ユーザーの {@link UUID} を取得する。
     *
     * @return ユーザーの {@link UUID}
     */
    @NotNull UUID getUniqueID();

    /**
     * ユーザーの名前を取得する。
     *
     * @return ユーザーの名前
     */
    @NotNull String getUserName();
}
