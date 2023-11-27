package net.okocraft.box.compatible.item;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.util.item.patcher.ItemDataPatcher;
import net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher;
import net.okocraft.box.storage.api.util.item.ItemVersion;
import net.okocraft.box.version.common.item.LegacyVersionPatches;
import org.jetbrains.annotations.NotNull;

public final class VersionAppendingPatcherFactory {

    public static @NotNull ItemNamePatcher createItemNamePatcher(@NotNull ItemVersion startingVersion) {
        var builder = new ItemNamePatcherBuilder();

        if (startingVersion.dataVersion().isBetween(MCDataVersion.MC_1_19, MCDataVersion.MC_1_19_4) && startingVersion.defaultItemProviderVersion() == 0) {
            builder.append(LegacyVersionPatches::goatHornName);
        }

        if (startingVersion.dataVersion().isBeforeOrSame(MCDataVersion.MC_1_20_2) && MCDataVersion.CURRENT.isAfter(MCDataVersion.MC_1_20_2)) {
            builder.append(LegacyVersionPatches::shortGrassName);
        }

        if (startingVersion.dataVersion().isBefore(MCDataVersion.CURRENT)) { // FIXME: 1.21
            builder.append(LegacyVersionPatches::potionName);
        }

        return builder.result;
    }

    public static @NotNull ItemDataPatcher createItemDataPatcher(@NotNull ItemVersion startingVersion) {
        var builder = new ItemDataPatcherBuilder();

        if (startingVersion.dataVersion().isAfterOrSame(MCDataVersion.MC_1_19)) {
            builder.append(LegacyVersionPatches::goatHornData);
        }

        return builder.result;
    }

    private static class ItemNamePatcherBuilder {
        private ItemNamePatcher result = ItemNamePatcher.NOOP;

        private void append(@NotNull ItemNamePatcher other) {
            if (other == ItemNamePatcher.NOOP) {
                return;
            }
            if (this.result == ItemNamePatcher.NOOP) {
                this.result = other;
            } else {
                this.result = this.result.andThen(other);
            }
        }
    }

    private static class ItemDataPatcherBuilder {
        private ItemDataPatcher result = ItemDataPatcher.NOOP;

        private void append(@NotNull ItemDataPatcher other) {
            if (other == ItemDataPatcher.NOOP) {
                return;
            }

            if (this.result == ItemDataPatcher.NOOP) {
                this.result = other;
            } else {
                this.result = this.result.andThen(other);
            }
        }
    }

    private VersionAppendingPatcherFactory() {
        throw new UnsupportedOperationException();
    }
}
