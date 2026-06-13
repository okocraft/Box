package net.okocraft.box.item;

import net.okocraft.box.api.util.MCDataVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public final class MinecraftVersioning {

    private static final Set<MCDataVersion> SUPPORTED_VERSIONS = Set.of(
        MCDataVersion.MC_1_21_2,
        MCDataVersion.MC_1_21_3,
        MCDataVersion.MC_1_21_4,
        MCDataVersion.MC_1_21_5,
        MCDataVersion.MC_1_21_6,
        MCDataVersion.MC_1_21_7,
        MCDataVersion.MC_1_21_8,
        MCDataVersion.MC_1_21_9,
        MCDataVersion.MC_1_21_10,
        MCDataVersion.MC_1_21_11,
        MCDataVersion.MC_26_1,
        MCDataVersion.MC_26_1_1,
        MCDataVersion.MC_26_1_2
    );

    public static @NotNull MCDataVersion leastVersion() {
        return SUPPORTED_VERSIONS.stream().min(Comparator.naturalOrder()).orElseThrow();
    }

    public static @NotNull DefaultItemProvider createDefaultItemProvider() {
        return new DefaultItemProviderImpl(findLatestVersion(MCDataVersion.current()));
    }

    public static @NotNull MCDataVersion findLatestVersion(@NotNull MCDataVersion current) {
        return SUPPORTED_VERSIONS.stream()
            .filter(version -> version.isBeforeOrSame(current))
            .max(Comparator.naturalOrder())
            .orElseThrow();
    }

    private record DefaultItemProviderImpl(@NotNull MCDataVersion version) implements DefaultItemProvider {

        @Override
        public @NotNull Stream<DefaultItem> provide() {
            return new ItemSources.Merger()
                .append(ItemSources.itemTypes())
                .append(ItemSources.potions())
                .append(ItemSources.enchantedBooks())
                .append(ItemSources.fireworks())
                .append(ItemSources.goatHorns())
                .append(ItemSources.ominousBottles())
                .result();
        }

        @Override
        public @NotNull Map<String, String> renamedItems(@NotNull MCDataVersion startingVersion, @NotNull MCDataVersion currentVersion) {
            return RenamedItems.loadFromResource(startingVersion, currentVersion);
        }

        @Override
        public @NotNull UnaryOperator<String> itemNameConvertor(@NotNull MCDataVersion startingVersion, @NotNull MCDataVersion currentVersion) {
            Map<String, String> renameMap = RenamedItems.loadFromResource(startingVersion, currentVersion);
            return name -> renameMap.getOrDefault(name, name);
        }
    }
}
