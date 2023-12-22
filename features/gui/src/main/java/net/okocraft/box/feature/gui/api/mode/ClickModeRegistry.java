package net.okocraft.box.feature.gui.api.mode;

import net.okocraft.box.feature.gui.internal.mode.StorageMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public final class ClickModeRegistry {

    private static final StorageMode STORAGE_MODE = new StorageMode((key, ignoredValue) -> key);
    private static final List<BoxItemClickMode> REGISTERED_BOX_ITEM_CLICK_MODE = new ArrayList<>(List.of(STORAGE_MODE));

    // Because ClickModeRegistry#getModes is called frequently,
    // keep a copied list to reduce calls to List#copyOf and Collections#unmodifiableList.
    private static List<BoxItemClickMode> COPIED_REGISTERED_BOX_ITEM_CLICK_MODE = List.copyOf(REGISTERED_BOX_ITEM_CLICK_MODE);

    public static @NotNull StorageMode getStorageMode() {
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
