package net.okocraft.box.storage.api.util.item;

import net.okocraft.box.api.util.MCDataVersion;
import org.jetbrains.annotations.NotNull;

public record ItemVersion(@NotNull MCDataVersion dataVersion, int defaultItemProviderVersion) {

    public boolean isTryingDowngrade(@NotNull ItemVersion other) {
        return other.dataVersion.isAfter(this.dataVersion) ||
                (other.dataVersion.isSame(this.dataVersion) && this.defaultItemProviderVersion < other.defaultItemProviderVersion);
    }

}
