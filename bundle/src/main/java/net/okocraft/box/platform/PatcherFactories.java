package net.okocraft.box.platform;

import net.okocraft.box.api.model.item.ItemVersion;
import net.okocraft.box.storage.api.util.item.patcher.ItemDataPatcher;
import net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher;
import net.okocraft.box.version.common.item.LegacyVersionPatches;
import org.jetbrains.annotations.NotNull;

import static net.okocraft.box.api.util.MCDataVersion.MC_1_19;
import static net.okocraft.box.api.util.MCDataVersion.MC_1_19_4;
import static net.okocraft.box.api.util.MCDataVersion.MC_1_20_3;
import static net.okocraft.box.api.util.MCDataVersion.MC_1_20_4;

final class PatcherFactories {

    static @NotNull ItemNamePatcher createItemNamePatcher(@NotNull ItemVersion startingVersion, @NotNull ItemVersion currentVersion) {
        var builder = new ItemNamePatcherBuilder();
        var dataVer = startingVersion.dataVersion();
        var itemVer = startingVersion.defaultItemVersion();

        if (dataVer.isBetween(MC_1_19, MC_1_19_4) && itemVer == 0) {
            builder.append(LegacyVersionPatches::goatHornName);
        }

        if (dataVer.isBefore(MC_1_20_3) && currentVersion.dataVersion().isAfterOrSame(MC_1_20_3)) {
            builder.append(LegacyVersionPatches::shortGrassName);
        }

        if (dataVer.isBefore(MC_1_20_4) && currentVersion.dataVersion().isAfterOrSame(MC_1_20_4)) { // TODO: back to MC_1_21 after Minecraft 1.21 released
            builder.append(LegacyVersionPatches::potionName);
        }

        return builder.result;
    }

    static @NotNull ItemDataPatcher createItemDataPatcher(@NotNull ItemVersion startingVersion, @NotNull ItemVersion ignoredCurrentVersion) {
        var builder = new ItemDataPatcherBuilder();
        var dataVer = startingVersion.dataVersion();
        var itemVer = startingVersion.defaultItemVersion();

        if (dataVer.isBetween(MC_1_19, MC_1_19_4) && itemVer == 0) {
            builder.append(LegacyVersionPatches::goatHorn);
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
                var current = this.result;
                this.result = original -> other.renameIfNeeded(current.renameIfNeeded(original));
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
                var current = this.result;
                this.result = original -> other.patch(current.patch(original));
            }
        }
    }

    private PatcherFactories() {
        throw new UnsupportedOperationException();
    }
}
