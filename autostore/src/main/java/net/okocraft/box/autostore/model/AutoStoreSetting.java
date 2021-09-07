package net.okocraft.box.autostore.model;

import net.okocraft.box.autostore.model.mode.AllModeSetting;
import net.okocraft.box.autostore.model.mode.AutoStoreMode;
import net.okocraft.box.autostore.model.mode.PerItemModeSetting;
import org.jetbrains.annotations.NotNull;

public class AutoStoreSetting {

    private final AllModeSetting allModeSetting = new AllModeSetting();
    private final PerItemModeSetting perItemModeSetting = new PerItemModeSetting();

    private AutoStoreMode currentMode = allModeSetting;

    public @NotNull AutoStoreMode getCurrentMode() {
        return currentMode;
    }

    public void setMode(@NotNull AutoStoreMode mode) {
        this.currentMode = mode;
    }

    public @NotNull AllModeSetting getAllModeSetting() {
        return allModeSetting;
    }

    public @NotNull PerItemModeSetting getPerItemModeSetting() {
        return perItemModeSetting;
    }
}
