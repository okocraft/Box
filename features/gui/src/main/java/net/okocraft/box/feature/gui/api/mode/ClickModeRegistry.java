package net.okocraft.box.feature.gui.api.mode;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClickModeRegistry {

    private static final List<BoxItemClickMode> REGISTERED_BOX_ITEM_CLICK_MODE = new ArrayList<>();
    private static List<BoxItemClickMode> COPIED_REGISTERED_BOX_ITEM_CLICK_MODE = Collections.emptyList();

    public static @NotNull @Unmodifiable List<BoxItemClickMode> getModes() {
        return getModes(BoxItemClickMode.GuiType.JAVA);
    }

    public static @NotNull @Unmodifiable List<BoxItemClickMode> getModes(BoxItemClickMode.GuiType guiType) {
        return COPIED_REGISTERED_BOX_ITEM_CLICK_MODE
                .stream()
                .filter(mode -> mode.getApplicableGuiTypes().contains(guiType))
                .toList();
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
