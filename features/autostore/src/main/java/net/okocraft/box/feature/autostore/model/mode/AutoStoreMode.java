package net.okocraft.box.feature.autostore.model.mode;

import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

public interface AutoStoreMode {

    @NotNull String getModeName();

    boolean isEnabled(@NotNull BoxItem item);

}
