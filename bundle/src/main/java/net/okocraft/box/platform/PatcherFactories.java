package net.okocraft.box.platform;

import net.okocraft.box.api.model.item.ItemVersion;
import net.okocraft.box.storage.api.util.item.patcher.ItemDataPatcher;
import net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher;
import net.okocraft.box.version.common.item.LegacyVersionPatches;
import net.okocraft.box.version.paper_1_20_5.Paper_1_20_5;
import org.jetbrains.annotations.NotNull;

final class PatcherFactories {

    static @NotNull ItemNamePatcher createItemNamePatcher(@NotNull ItemVersion startingVersion, @NotNull ItemVersion currentVersion) {
        var builder = new ItemNamePatcherBuilder();

        if (LegacyVersionPatches.shouldPatchGoatHorn(startingVersion)) {
            builder.append(LegacyVersionPatches::goatHornName);
        }

        if (LegacyVersionPatches.shouldPatchShortGrassName(startingVersion, currentVersion)) {
            builder.append(LegacyVersionPatches::shortGrassName);
        }

        if (LegacyVersionPatches.shouldPatchPotionName(startingVersion, currentVersion)) {
            builder.append(LegacyVersionPatches::potionName);
        }

        if (startingVersion.isBefore(Paper_1_20_5.VERSION)) {
            builder.append(Paper_1_20_5::turtleScute);
        }

        return builder.result;
    }

    static @NotNull ItemDataPatcher createItemDataPatcher(@NotNull ItemVersion startingVersion, @NotNull ItemVersion ignoredCurrentVersion) {
        var builder = new ItemDataPatcherBuilder();

        if (LegacyVersionPatches.shouldPatchGoatHorn(startingVersion)) {
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
