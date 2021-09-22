package net.okocraft.box.feature.autostore.model.mode;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

public class AllModeSetting implements AutoStoreMode {

    @Override
    public @NotNull String getModeName() {
        return "all";
    }

    @Override
    public boolean isEnabled(@NotNull BoxItem item) {
        return true;
    }
}
