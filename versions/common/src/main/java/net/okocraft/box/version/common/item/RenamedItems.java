package net.okocraft.box.version.common.item;

import it.unimi.dsi.fastutil.Pair;
import net.okocraft.box.api.util.MCDataVersion;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class RenamedItems {

    private static final List<MCDataVersion> VERSIONS = List.of(
        MCDataVersion.MC_1_20_5,
        MCDataVersion.MC_1_21
    );

    public static @NotNull Map<String, String> loadFromResource(@NotNull MCDataVersion startingVersion, @NotNull MCDataVersion currentVersion) {
        return VERSIONS.stream()
            .filter(version -> startingVersion.isBefore(version) && currentVersion.isAfterOrSame(version))
            .sorted()
            .map(RenamedItems::loadVersionFromResource)
            .map(Map::entrySet)
            .flatMap(Collection::stream)
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (older, newer) -> newer));
    }

    public static @NotNull Map<String,String > loadVersionFromResource(@NotNull MCDataVersion version) {
        try (InputStream in = RenamedItems.class.getClassLoader().getResourceAsStream(version.dataVersion() + ".txt")) {
            if (in == null) {
                return Collections.emptyMap();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return reader.lines()
                    .filter(line -> !line.startsWith("#"))
                    .map(line -> {
                        int separatorIndex = line.indexOf(":");
                        return separatorIndex != -1 && separatorIndex + 1 != line.length() ?
                            Pair.of(line.substring(0, separatorIndex), line.substring(separatorIndex + 1)) :
                            null;
                    })
                    .filter(Objects::nonNull)
                    .filter(Predicate.not(pair -> pair.first().isEmpty() && pair.second().isEmpty()))
                    .collect(Collectors.toUnmodifiableMap(Pair::left, Pair::right));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
