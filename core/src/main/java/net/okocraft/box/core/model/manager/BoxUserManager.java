package net.okocraft.box.core.model.manager;

import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.core.util.executor.InternalExecutors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class BoxUserManager implements UserManager {

    private final UserStorage userStorage;
    private final ExecutorService executor;

    public BoxUserManager(@NotNull UserStorage userStorage) {
        this.userStorage = userStorage;
        this.executor = InternalExecutors.newSingleThreadExecutor("User Manager");
    }

    @Override
    public @NotNull BoxUser createBoxUser(@NotNull UUID uuid) {
        return BoxUserFactory.create(uuid);
    }

    @Override
    public @NotNull BoxUser createBoxUser(@NotNull UUID uuid, @NotNull String name) {
        return BoxUserFactory.create(uuid, name);
    }

    @Override
    public @NotNull BoxUser loadBoxUser(@NotNull UUID uuid) {
        return loadUser(uuid).join();
    }

    @Override
    public @Nullable BoxUser searchByName(@NotNull String name) {
        return search(name).join().orElse(null);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull BoxUser> loadUser(@NotNull UUID uuid) {
        Objects.requireNonNull(uuid);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return userStorage.getUser(uuid);
            } catch (Exception e) {
                throw new RuntimeException("Could not load the box user (" + uuid + ")", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveUser(@NotNull BoxUser boxUser) {
        Objects.requireNonNull(boxUser);

        return CompletableFuture.runAsync(() -> {
            try {
                userStorage.saveBoxUser(boxUser);
            } catch (Exception e) {
                throw new RuntimeException("Could not save the box user (" + boxUser.getUUID() + ")", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveUserIfNotExists(@NotNull BoxUser boxUser) {
        Objects.requireNonNull(boxUser);

        return CompletableFuture.runAsync(() -> {
            try {
                userStorage.saveBoxUserIfNotExists(boxUser);
            } catch (Exception e) {
                throw new RuntimeException("Could not save the box user (" + boxUser.getUUID() + ")", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Optional<BoxUser>> search(@NotNull String name) {
        Objects.requireNonNull(name);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return userStorage.search(name);
            } catch (Exception e) {
                throw new RuntimeException("Could not search for the box user (" + name + ")", e);
            }
        }, executor);
    }
}
