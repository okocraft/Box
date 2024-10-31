package net.okocraft.box.test.shared.util;

import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.core.serialization.key.KeyGenerator;
import dev.siroshun.configapi.core.serialization.record.RecordDeserializer;
import dev.siroshun.configapi.format.yaml.YamlFormat;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public final class TestCaseLoader {

    public static <R extends Record> @NotNull Collection<R> loadFromResource(@NotNull Class<R> clazz, @NotNull String name) throws IOException {
        try (var input = clazz.getClassLoader().getResourceAsStream(name)) {
            return readTestCases(clazz, YamlFormat.DEFAULT.load(Objects.requireNonNull(input, "resource not found: " + name)));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static <R extends Record> @NotNull Collection<R> readTestCases(@NotNull Class<R> clazz, @NotNull MapNode source) {
        var deserializer = RecordDeserializer.create(clazz, KeyGenerator.CAMEL_TO_KEBAB);
        return source.getList("cases").asList(MapNode.class).stream().map(deserializer::deserialize).toList();
    }

    private TestCaseLoader() {
        throw new UnsupportedOperationException();
    }
}
