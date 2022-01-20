package net.okocraft.box.feature.gui.api.mode;

import net.okocraft.box.feature.gui.internal.mode.StorageMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClickModeRegistry {

    private static final BoxItemClickMode STORAGE_MODE = new StorageMode();
    private static final List<BoxItemClickMode> REGISTERED_BOX_ITEM_CLICK_MODE = new ArrayList<>(List.of(STORAGE_MODE));

    private static List<BoxItemClickMode> COPIED_REGISTERED_BOX_ITEM_CLICK_MODE = Collections.emptyList();

    public static @NotNull BoxItemClickMode getStorageMode() {
        return STORAGE_MODE;
    }

    public static @NotNull @Unmodifiable List<BoxItemClickMode> getModes() {
        return COPIED_REGISTERED_BOX_ITEM_CLICK_MODE;
    }

    public static void register(@NotNull BoxItemClickMode mode) {
        REGISTERED_BOX_ITEM_CLICK_MODE.add(mode);

        COPIED_REGISTERED_BOX_ITEM_CLICK_MODE = List.copyOf(REGISTERED_BOX_ITEM_CLICK_MODE);
    }

    public static void unregister(@NotNull BoxItemClickMode mode) {
        if (REGISTERED_BOX_ITEM_CLICK_MODE.remove(mode)) {
            COPIED_REGISTERED_BOX_ITEM_CLICK_MODE = List.copyOf(REGISTERED_BOX_ITEM_CLICK_MODE);
        }
    }
}
