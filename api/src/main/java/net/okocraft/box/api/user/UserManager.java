package net.okocraft.box.api.user;

import net.okocraft.box.api.util.Result;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * ユーザー管理インターフェース。
 * <p>
 * このインターフェースのいくつかのメソッド(特に IO を経るもの)の返り値は {@link CompletableFuture} であるとし、要求は非同期で処理される。
 */
public interface UserManager {

    /**
     * 指定した {@link UUID} のユーザーインスタンスを取得する。
     *
     * @param uuid ユーザーの {@link UUID}
     * @return 将来完了するユーザーのロードタスク
     */
    @NotNull CompletableFuture<User> loadUser(@NotNull UUID uuid);

    /**
     * 指定した名前のユーザーインスタンスを取得する。
     *
     * @param username ユーザーの名前
     * @return 将来完了するユーザーの検索・ロードタスク
     */
    @NotNull CompletableFuture<Optional<User>> loadUserByUserName(@NotNull String username);

    /**
     * 指定したユーザーを保存する。
     *
     * @param user 保存するユーザー
     * @return 将来完了するユーザーの保存タスク
     */
    @NotNull CompletableFuture<Result> saveUser(@NotNull User user);

    /**
     * 指定した {@link UUID} のユーザーがロードされているか。
     *
     * @param uuid 判定するユーザーの {@link UUID}
     * @return ロードされていれば {@code true}, そうでなければ {@code false}
     */
    boolean isLoaded(@NotNull UUID uuid);

    /**
     * 指定したユーザーをアンロードする。
     * <p>
     * このメソッドでは保存処理は行われない。
     *
     * @param user アンロードするユーザー
     */
    void unload(@NotNull User user);
}
