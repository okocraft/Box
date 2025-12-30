package net.okocraft.box.bundle;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import net.okocraft.box.storage.api.model.item.provider.DefaultItemProvider;
import net.okocraft.box.version.common.item.RenamedItems;
import net.okocraft.box.version.common.version.Versioned;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class VersionedImpls {

    public static VersionedImpls load(ClassLoader classLoader) {
        return new VersionedImpls(Versioned.implementations(classLoader));
    }

    private final Collection<Versioned> impls;

    private VersionedImpls(@NotNull Collection<Versioned> impls) {
        this.impls = impls;
    }

    public @NotNull MCDataVersion leastVersion() {
        return this.impls.stream()
            .min(Comparator.comparing(Versioned::version))
            .map(Versioned::version)
            .orElseThrow();
    }

    public @NotNull DefaultItemProvider createDefaultItemProvider(@NotNull MCDataVersion current) {
        Versioned latest = this.findLatest(current);
        return new DefaultItemProviderImpl(latest.version(), latest::defaultItems, RenamedItems::loadFromResource, this::createItemNameConvertor);
    }

    private @NotNull Versioned findLatest(@NotNull MCDataVersion current) {
        return this.impls.stream()
            .filter(impl -> impl.version().isBeforeOrSame(current))
            .max(Comparator.comparing(Versioned::version))
            .orElseThrow();
    }

    private @NotNull UnaryOperator<String> createItemNameConvertor(@NotNull MCDataVersion startingVersion, @NotNull MCDataVersion currentVersion) {
        Map<String, String> renameMap = RenamedItems.loadFromResource(startingVersion, currentVersion);
        return name -> renameMap.getOrDefault(name, name);
    }

    private record DefaultItemProviderImpl(@NotNull MCDataVersion version,
                                           @NotNull Supplier<Stream<DefaultItem>> defaultItemProvider,
                                           @NotNull BiFunction<MCDataVersion, MCDataVersion, Map<String, String>> renamedItemsProvider,
                                           @NotNull BiFunction<MCDataVersion, MCDataVersion, UnaryOperator<String>> itemNameConvertorFactory) implements DefaultItemProvider {
        @Override
        public @NotNull Stream<DefaultItem> provide() {
            return this.defaultItemProvider.get();
        }

        @Override
        public @NotNull Map<String, String> renamedItems(@NotNull MCDataVersion startingVersion, @NotNull MCDataVersion currentVersion) {
            return this.renamedItemsProvider.apply(startingVersion, currentVersion);
        }

        @Override
        public @NotNull UnaryOperator<String> itemNameConvertor(@NotNull MCDataVersion startingVersion, @NotNull MCDataVersion currentVersion) {
            return this.itemNameConvertorFactory.apply(startingVersion, currentVersion);
        }
    }
}
