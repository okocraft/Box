package net.okocraft.box.bundle;

import net.okocraft.box.api.model.item.ItemVersion;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.util.item.DefaultItem;
import net.okocraft.box.storage.api.util.item.DefaultItemProvider;
import net.okocraft.box.storage.api.util.item.patcher.ItemDataPatcher;
import net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher;
import net.okocraft.box.storage.api.util.item.patcher.PatcherFactory;
import net.okocraft.box.version.common.item.LegacyVersionPatches;
import net.okocraft.box.version.common.version.Versioned;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class VersionedImpls {

    private static final boolean PRINT_VERSION = false;

    public static VersionedImpls load(ClassLoader classLoader) {
        return new VersionedImpls(Versioned.implementations(classLoader));
    }

    private final Collection<Versioned> impls;

    private VersionedImpls(@NotNull Collection<Versioned> impls) {
        this.impls = impls;
    }

    public @NotNull ItemVersion leastVersion() {
        return this.impls.stream()
                .min(Comparator.comparing(Versioned::defaultItemVersion))
                .map(Versioned::defaultItemVersion)
                .orElseThrow();
    }

    public @NotNull DefaultItemProvider createDefaultItemProvider(@NotNull MCDataVersion current) {
        var latest = this.findLatest(current);
        this.debugVersion("Latest", latest);
        return new DefaultItemProviderImpl(latest.defaultItemVersion(), latest::defaultItems, this.createItemNamePatcherFactory(), this.createItemDataPatcherFactory());
    }

    private @NotNull Versioned findLatest(@NotNull MCDataVersion current) {
        return this.impls.stream()
                .filter(impl -> impl.defaultItemVersion().dataVersion().isBeforeOrSame(current))
                .max(Comparator.comparing(Versioned::defaultItemVersion))
                .orElseThrow();
    }

    private void addNeededItemNamePatches(@NotNull ItemVersion starting, @NotNull ItemNamePatcherBuilder builder) {
        this.impls.stream()
                .filter(impl -> starting.isBefore(impl.defaultItemVersion()))
                .sorted(Comparator.comparing(Versioned::defaultItemVersion))
                .peek(impl -> this.debugVersion("ItemNamePatches", impl))
                .flatMap(Versioned::itemNamePatchers)
                .forEach(builder::append);
    }

    private void addNeededItemDataPatches(@NotNull ItemVersion starting, @NotNull ItemDataPatcherBuilder builder) {
        this.impls.stream()
                .filter(impl -> starting.isBefore(impl.defaultItemVersion()))
                .sorted(Comparator.comparing(Versioned::defaultItemVersion))
                .peek(impl -> this.debugVersion("ItemDataPatches", impl))
                .flatMap(Versioned::itemDataPatchers)
                .forEach(builder::append);
    }

    private PatcherFactory<ItemNamePatcher> createItemNamePatcherFactory() {
        return (startingVersion, currentVersion) -> {
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

            this.addNeededItemNamePatches(startingVersion, builder);

            return builder.result;
        };
    }

    private PatcherFactory<ItemDataPatcher> createItemDataPatcherFactory() {
        return (startingVersion, currentVersion) -> {
            var builder = new ItemDataPatcherBuilder();

            if (LegacyVersionPatches.shouldPatchGoatHorn(startingVersion)) {
                builder.append(LegacyVersionPatches::goatHorn);
            }

            this.addNeededItemDataPatches(startingVersion, builder);

            return builder.result;
        };
    }

    private void debugVersion(@NotNull String name, @NotNull Versioned impl) {
        if (PRINT_VERSION) {
            var version = impl.defaultItemVersion();
            BoxLogger.logger().info("{}: {}, {} ({})", name, version.dataVersion(), version.defaultItemVersion(), impl.getClass().getSimpleName());
        }
    }

    private record DefaultItemProviderImpl(@NotNull ItemVersion version,
                                           @NotNull Supplier<Stream<DefaultItem>> defaultItemProvider,
                                           @NotNull PatcherFactory<ItemNamePatcher> itemNamePatcherFactory,
                                           @NotNull PatcherFactory<ItemDataPatcher> itemDataPatcherFactory) implements DefaultItemProvider {
        @Override
        public @NotNull Stream<DefaultItem> provide() {
            return this.defaultItemProvider.get();
        }
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
}
