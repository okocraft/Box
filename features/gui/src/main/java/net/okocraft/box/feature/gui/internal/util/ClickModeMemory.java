package net.okocraft.box.feature.gui.internal.util;

import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClickModeMemory {

    private static final Map<UUID, BoxItemClickMode> CLICK_MODE_MAP = new HashMap<>();

    public static @NotNull BoxItemClickMode getMode(@NotNull Player player) {
        return CLICK_MODE_MAP.getOrDefault(player.getUniqueId(), ClickModeRegistry.getModes().get(0));
    }

    public static void changeMode(@NotNull Player player, @NotNull BoxItemClickMode mode) {
        CLICK_MODE_MAP.put(player.getUniqueId(), mode);
    }

    private ClickModeMemory() {
        throw new UnsupportedOperationException();
    }
}
