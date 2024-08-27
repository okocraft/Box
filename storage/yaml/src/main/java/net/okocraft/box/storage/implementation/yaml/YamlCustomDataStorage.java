package net.okocraft.box.storage.implementation.yaml;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;

public class YamlCustomDataStorage implements CustomDataStorage {

    private final Path customDataDirectory;

    public YamlCustomDataStorage(@NotNull Path rootDirectory) {
        this.customDataDirectory = rootDirectory.resolve("custom-data");
    }

    public void init() throws IOException {
        Files.createDirectories(this.customDataDirectory);
    }

    @Override
    public @NotNull MapNode loadData(@NotNull Key key) throws Exception {
        var filepath = this.createFilepathFromKey(key);

        if (Files.isRegularFile(filepath)) {
            return YamlFormat.DEFAULT.load(filepath);
        } else {
            return MapNode.create();
        }
    }

    @Override
    public void saveData(@NotNull Key key, @NotNull MapNode mapNode) throws Exception {
        var filepath = this.createFilepathFromKey(key);

        if (mapNode.value().isEmpty()) {
            Files.deleteIfExists(filepath);
        } else {
            var parent = filepath.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                Files.createDirectories(parent);
            }
            YamlFormat.DEFAULT.save(mapNode, filepath);
        }
    }

    @Override
    public void visitData(@NotNull String namespace, @NotNull BiConsumer<Key, MapNode> consumer) throws Exception {
        if (!Key.parseableNamespace(namespace)) {
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        }

        try (var listStream = Files.list(this.customDataDirectory)) {
            listStream.filter(Files::isDirectory)
                .filter(path -> path.getFileName().toString().equals(namespace))
                .forEach(dir -> {
                    try {
                        Files.walkFileTree(dir, new YamlFileVisitor(dir, consumer));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override
    public void visitAllData(@NotNull BiConsumer<Key, MapNode> consumer) throws Exception {
        try (var listStream = Files.list(this.customDataDirectory)) {
            listStream.filter(Files::isDirectory)
                .forEach(dir -> {
                    try {
                        Files.walkFileTree(dir, new YamlFileVisitor(dir, consumer));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private @NotNull Path createFilepathFromKey(@NotNull Key key) {
        return this.customDataDirectory.resolve(key.namespace()).resolve(key.value() + ".yml");
    }

    private static class YamlFileVisitor implements FileVisitor<Path> {

        private final String namespace;
        private final BiConsumer<Key, MapNode> consumer;
        private final Path rootDir;

        private YamlFileVisitor(@NotNull Path rootDir, @NotNull BiConsumer<Key, MapNode> consumer) {
            this.rootDir = rootDir;
            this.namespace = rootDir.getFileName().toString();
            this.consumer = consumer;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @SuppressWarnings("PatternValidation")
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            var relative = this.rootDir.relativize(file).toString().replace(File.separatorChar, '/');

            if (!relative.endsWith(".yml")) {
                return FileVisitResult.CONTINUE;
            }

            var value = relative.substring(0, relative.length() - 4);

            if (Key.checkValue(value).isEmpty()) {
                this.consumer.accept(Key.key(this.namespace, value), YamlFormat.DEFAULT.load(file));
            } else {
                BoxLogger.logger().warn("Invalid value: {}", value);
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            throw exc;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }
}
