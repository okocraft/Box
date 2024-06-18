package net.okocraft.box.version.common.version;

import it.unimi.dsi.fastutil.Pair;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItem;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Versioned {

    static List<Versioned> implementations(ClassLoader classLoader) {
        return ServiceLoader.load(Versioned.class, classLoader).stream().map(ServiceLoader.Provider::get).toList();
    }

    MCDataVersion version();

    Stream<DefaultItem> defaultItems();

    default @NotNull Map<String, String> loadRenamedItems() {
        try (var in = this.getClass().getClassLoader().getResourceAsStream(this.version().dataVersion() + ".txt")) {
            if (in == null) {
                return Collections.emptyMap();
            }

            try (var reader = new BufferedReader(new InputStreamReader(in))) {
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
