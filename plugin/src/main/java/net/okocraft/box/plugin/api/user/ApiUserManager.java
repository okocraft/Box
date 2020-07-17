package net.okocraft.box.plugin.api.user;

import net.okocraft.box.api.user.User;
import net.okocraft.box.api.user.UserManager;
import net.okocraft.box.api.util.Result;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApiUserManager implements UserManager {

    @Override
    public @NotNull CompletableFuture<User> loadUser(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<String> name = Optional.ofNullable(Bukkit.getOfflinePlayer(uuid).getName());
            return new ApiUser(uuid, name.orElse("UNKNOWN"));
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull CompletableFuture<Optional<User>> loadUserByUserName(@NotNull String username) {
        return CompletableFuture.supplyAsync(() -> {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
            return Optional.of(new ApiUser(offlinePlayer.getUniqueId(), username));
        });
    }

    @Override
    public @NotNull CompletableFuture<Result> saveUser(@NotNull User user) {
        return CompletableFuture.completedFuture(Result.SUCCESS); // 現在、データ操作毎に保存される仕様になっている。
    }

    @Override
    public boolean isLoaded(@NotNull UUID uuid) {
        return Bukkit.getPlayer(uuid) != null; // ログインしている間は常にロードされており、オフラインの場合 DB を直接操作する。
    }

    @Override
    public void unload(@NotNull User user) {
        // TODO: API キャッシュからの削除
    }
}
