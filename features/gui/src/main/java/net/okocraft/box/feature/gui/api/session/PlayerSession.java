package net.okocraft.box.feature.gui.api.session;

import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import net.okocraft.box.feature.gui.api.mode.GuiType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerSession {

    private static final Map<UUID, PlayerSession> SESSION_MAP = new HashMap<>();

    public static @NotNull PlayerSession get(@NotNull Player player) {
        return SESSION_MAP.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerSession(player));
    }

    private final Player viewer;
    private BoxItemClickMode currentClickMode;
    private final GuiType defaultGuiType;

    private final Map<String, CustomNumberHolder> customNumberMap = new HashMap<>();
    private List<BoxItemClickMode> availableClickModes;
    private @Nullable StockHolder stockHolder;

    private PlayerSession(Player viewer) {
        this.viewer = viewer;
        this.defaultGuiType = viewer.getUniqueId().toString().startsWith("00000000")
                ? GuiType.BE
                : GuiType.JAVA;
        this.availableClickModes = ClickModeRegistry.getModes(defaultGuiType);
        this.currentClickMode = availableClickModes.isEmpty() ? null : availableClickModes.get(0);
    }

    public @NotNull GuiType getDefaultGuiType() {
        return defaultGuiType;
    }

    public @NotNull BoxItemClickMode getBoxItemClickMode() {
        return currentClickMode;
    }

    public void setBoxItemClickMode(@NotNull BoxItemClickMode boxItemClickMode) {
        this.currentClickMode = Objects.requireNonNull(boxItemClickMode);
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
        return availableClickModes != null
                ? availableClickModes.stream().filter(mode -> mode.canUse(viewer)).toList()
                : Collections.emptyList();
    }

    public void setAvailableClickModes(@NotNull List<BoxItemClickMode> availableClickModes) {
        this.availableClickModes = Objects.requireNonNull(availableClickModes);
    }
}
