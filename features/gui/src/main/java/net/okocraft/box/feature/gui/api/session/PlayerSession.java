package net.okocraft.box.feature.gui.api.session;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSession {

    public static @NotNull PlayerSession newSession(@NotNull Player player) {
        return newSession(player, BoxProvider.get().getBoxPlayerMap().get(player));
    }

    public static @NotNull PlayerSession newSession(@NotNull Player player, @NotNull BoxPlayer source) {
        var modes = ClickModeRegistry.getModes().stream().filter(mode -> mode.canUse(player, source)).toList();
        return new PlayerSession(player, source, modes);
    }

    private final Map<TypedKey<?>, Object> dataMap = new ConcurrentHashMap<>();

    private final Player viewer;
    private final BoxPlayer source;

    private List<BoxItemClickMode> availableClickModes;
    private @Nullable BoxItemClickMode currentClickMode;
    private @Nullable StockHolder stockHolder;
    private @Nullable PreviousMenu previousMenu;

    public PlayerSession(@NotNull Player viewer, @NotNull BoxPlayer source, @NotNull List<BoxItemClickMode> availableClickModes) {
        this.viewer = viewer;
        this.source = source;
        this.availableClickModes = availableClickModes;

        if (this.availableClickModes.isEmpty()) {
            throw new IllegalStateException("No click mode available.");
        }
    }

    public @NotNull BoxItemClickMode getBoxItemClickMode() {
        return Objects.requireNonNullElse(currentClickMode, getAvailableClickModes().get(0));
    }

    public void setBoxItemClickMode(@Nullable BoxItemClickMode boxItemClickMode) {
        this.currentClickMode = boxItemClickMode;
    }

    public @NotNull StockHolder getStockHolder() {
        return stockHolder != null ? stockHolder : source.getCurrentStockHolder();
    }

    public void setStockHolder(@Nullable StockHolder stockHolder) {
        this.stockHolder = stockHolder;
    }

    public @NotNull @Unmodifiable List<BoxItemClickMode> getAvailableClickModes() {
        return availableClickModes;
    }

    public void setAvailableClickModes(@NotNull List<BoxItemClickMode> availableClickModes) {
        if (availableClickModes.isEmpty()) {
            throw new IllegalStateException("No click mode available.");
        }

        this.availableClickModes = availableClickModes;
    }

    public @NotNull Player getViewer() {
        return viewer;
    }

    public @NotNull BoxPlayer getSource() {
        return source;
    }

    public <T> void putData(@NotNull TypedKey<T> key, @NotNull T data) {
        dataMap.put(key, data);
    }

    public <T> @Nullable T getData(@NotNull TypedKey<T> key) {
        var data = dataMap.get(key);
        return key.clazz().isInstance(data) ? key.clazz().cast(data) : null;
    }

    public <T> @Nullable T removeData(@NotNull TypedKey<T> key) {
        var removed = dataMap.remove(key);
        return key.clazz().isInstance(removed) ? key.clazz().cast(removed) : null;
    }

    public <T> @NotNull T getDataOrThrow(@NotNull TypedKey<T> key) {
        var data = getData(key);

        if (data == null) {
            throw new IllegalStateException(key + " does not exist in this session (" + getViewer().getName() + ")");
        }

        return data;
    }

    public boolean hasPreviousMenu() {
        return this.previousMenu != null;
    }

    public @NotNull Menu backMenu() {
        if (this.previousMenu == null) {
            throw new IllegalStateException("No previous menu.");
        }

        var menu = this.previousMenu.menu;
        this.previousMenu = this.previousMenu.parent;

        return menu;
    }

    public void rememberMenu(@NotNull Menu menu) {
        this.previousMenu = new PreviousMenu(menu, this.previousMenu);
    }

    private record PreviousMenu(@NotNull Menu menu, @Nullable PreviousMenu parent) {
    }
}
