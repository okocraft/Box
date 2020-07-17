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

    @NotNull CompletableFuture<User> loadUser(@NotNull UUID uuid);

    @NotNull CompletableFuture<Optional<User>> loadUserByUserName(@NotNull String username);

    @NotNull CompletableFuture<Result> saveUser(@NotNull User user);

    boolean isLoaded(@NotNull UUID uuid);

    void unload(@NotNull User user);
}
