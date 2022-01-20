package net.okocraft.box.feature.gui.api.session;

import net.okocraft.box.api.model.stock.StockHolder;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerSession {

    private static final Map<UUID, PlayerSession> SESSION_MAP = new HashMap<>();

    public static @NotNull PlayerSession get(@NotNull Player player) {
        return SESSION_MAP.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerSession(
                player.getUniqueId().toString().startsWith("00000000")
                        ? BoxItemClickMode.GuiType.BE
                        : BoxItemClickMode.GuiType.JAVA
        ));
    }

    private BoxItemClickMode currentClickMode;

    private final Map<String, CustomNumberHolder> customNumberMap = new HashMap<>();
    private @Nullable StockHolder stockHolder;

    private PlayerSession(BoxItemClickMode.GuiType defaultGuiType) {
        this.currentClickMode = ClickModeRegistry.getModes(defaultGuiType).get(0);
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
}
