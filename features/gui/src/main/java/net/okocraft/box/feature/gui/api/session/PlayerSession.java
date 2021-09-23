package net.okocraft.box.feature.gui.api.session;

import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerSession {

    private static final Map<UUID, PlayerSession> SESSION_MAP = new HashMap<>();

    public static @NotNull PlayerSession get(@NotNull Player player) {
        return SESSION_MAP.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerSession());
    }

    private BoxItemClickMode currentClickMode = ClickModeRegistry.getModes().get(0);

    private final Map<String, CustomNumberHolder> customNumberMap = new HashMap<>();

    private PlayerSession() {
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
}
