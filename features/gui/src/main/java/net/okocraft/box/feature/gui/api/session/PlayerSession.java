package net.okocraft.box.feature.gui.api.session;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PlayerSession {

    public static @NotNull PlayerSession newSession(@NotNull Player viewer) {
        BoxPlayer boxPlayer = BoxAPI.api().getBoxPlayerMap().get(viewer);
        return new PlayerSession(viewer, boxPlayer.asUser(), boxPlayer.getCurrentStockHolder());
    }

    public static @NotNull PlayerSession newSession(@NotNull Player viewer, @NotNull BoxUser sourceUser) {
        return new PlayerSession(viewer, sourceUser, BoxAPI.api().getStockManager().getPersonalStockHolder(sourceUser));
    }

    private final Player viewer;
    private final BoxUser sourceUser;
    private final StockHolder sourceStockHolder;
    private final Map<TypedKey<?>, Object> dataMap = new ConcurrentHashMap<>();

    private PlayerSession(@NotNull Player viewer, @NotNull BoxUser sourceUser, @NotNull StockHolder sourceStockHolder) {
        this.viewer = viewer;
        this.sourceUser = sourceUser;
        this.sourceStockHolder = sourceStockHolder;
    }

    public @NotNull Player getViewer() {
        return this.viewer;
    }

    public @NotNull BoxUser getSourceUser() {
        return this.sourceUser;
    }

    public @NotNull StockHolder getSourceStockHolder() {
        return this.sourceStockHolder;
    }

    public <T> @Nullable T getData(@NotNull TypedKey<T> key) {
        Object data = this.dataMap.get(key);
        return key.clazz().isInstance(data) ? key.clazz().cast(data) : null;
    }

    public <T> @NotNull T getDataOrThrow(@NotNull TypedKey<T> key) {
        T data = this.getData(key);

        if (data == null) {
            throw new IllegalStateException(key + " does not exist in this session (" + this.getViewer().getName() + ")");
        }

        return data;
    }

    public <T> void putData(@NotNull TypedKey<T> key, @NotNull T data) {
        this.dataMap.put(key, data);
    }

    public <T> @NotNull T computeDataIfAbsent(@NotNull TypedKey<T> key, @NotNull Supplier<? extends T> supplier) {
        Object data = this.dataMap.computeIfAbsent(key, ignored -> supplier.get());
        if (key.clazz().isInstance(data)) {
            return key.clazz().cast(data);
        } else {
            T created = supplier.get();
            this.dataMap.put(key, data);
            return created;
        }
    }

    public <T> @Nullable T removeData(@NotNull TypedKey<T> key) {
        Object removed = this.dataMap.remove(key);
        return key.clazz().isInstance(removed) ? key.clazz().cast(removed) : null;
    }
}
