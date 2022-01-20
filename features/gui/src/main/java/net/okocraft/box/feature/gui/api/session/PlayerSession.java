package net.okocraft.box.feature.gui.api.session;

import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerSession {

    private static final Map<UUID, PlayerSession> SESSION_MAP = new HashMap<>();

    public static @NotNull PlayerSession get(@NotNull Player player) {
        return SESSION_MAP.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerSession());
    }

    private final Map<String, CustomNumberHolder> customNumberMap = new HashMap<>();
    private List<BoxItemClickMode> availableClickModes;
    private @Nullable BoxItemClickMode currentClickMode;
    private @Nullable StockHolder stockHolder;

    private PlayerSession() {
    }

    public @NotNull BoxItemClickMode getBoxItemClickMode() {
        return Objects.requireNonNullElse(currentClickMode, getAvailableClickModes().get(0));
    }

    public void setBoxItemClickMode(@Nullable BoxItemClickMode boxItemClickMode) {
        this.currentClickMode = boxItemClickMode;
    }

    public @NotNull CustomNumberHolder getCustomNumberHolder(@NotNull String numberName) {
        return customNumberMap.computeIfAbsent(numberName, k -> new CustomNumberHolder());
    }

    public void resetCustomNumbers() {
        customNumberMap.values().forEach(num -> num.setAmount(1));
    }

    public @NotNull StockHolder getStockHolder() {
        if (stockHolder != null) {
            return stockHolder;
        } else {
            throw new IllegalStateException("The stockholder is not set");
        }
    }

    public void setStockHolder(@Nullable StockHolder stockHolder) {
        this.stockHolder = stockHolder;
    }

    public @NotNull @Unmodifiable List<BoxItemClickMode> getAvailableClickModes() {
        return availableClickModes != null && !availableClickModes.isEmpty() ?
                availableClickModes : List.of(ClickModeRegistry.getStorageMode());
    }

    public void setAvailableClickModes(@NotNull List<BoxItemClickMode> availableClickModes) {
        this.availableClickModes = Objects.requireNonNull(availableClickModes);
    }
}
