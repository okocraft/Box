package net.okocraft.box.storage.implementation.yaml;

import dev.siroshun.configapi.core.node.NullNode;
import dev.siroshun.configapi.core.node.StringRepresentable;
import dev.siroshun.configapi.format.yaml.YamlFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.okocraft.box.storage.api.model.item.DefaultItemStorage;
import net.okocraft.box.storage.api.model.item.NamedItem;
import net.okocraft.box.storage.api.util.SneakyThrow;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

class YamlDefaultItemStorage implements DefaultItemStorage {

    private final Path directory;
    private final YamlMetaStorage metaStorage;
    private final Path filepath;

    YamlDefaultItemStorage(@NotNull Path rootDirectory, @NotNull YamlMetaStorage metaStorage) {
        this.directory = rootDirectory.resolve("items");
        this.metaStorage = metaStorage;
        this.filepath = this.directory.resolve("default-items.yml");
    }

    @Override
    public int newDefaultItemId(@NotNull String name) throws Exception {
        if (!Files.isDirectory(this.directory)) {
            Files.createDirectories(this.directory);
        }

        int id = this.metaStorage.newItemId();

        try (var writer = Files.newBufferedWriter(this.filepath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            appendNewItem(writer, id, name);
        }

        return id;
    }

    @Override
    public @NotNull <I extends NamedItem<?>, R> List<R> initializeDefaultItems(@NotNull Stream<I> defaultItemStream, @NotNull BiFunction<I, Integer, R> function) throws Exception {
        if (!Files.isDirectory(this.directory)) {
            Files.createDirectories(this.directory);
        }

        List<R> result;

        try (var writer = Files.newBufferedWriter(this.filepath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            result = defaultItemStream.map(item -> {
                int id = this.metaStorage.newItemIdWithoutSaving();
                try {
                    appendNewItem(writer, id, item.plainName());
                } catch (IOException e) {
                    SneakyThrow.sneaky(e);
                }
                return function.apply(item, id);
            }).toList();
        }

        this.metaStorage.saveLastItemId();
        return result;
    }

    @Override
    public @NotNull Object2IntMap<String> loadDefaultItemNameToIdMap() throws Exception {
        var result = new Object2IntOpenHashMap<String>();

        if (!Files.isRegularFile(this.filepath)) {
            return result;
        }

        var source = YamlFormat.DEFAULT.load(this.filepath);

        for (var entry : source.value().entrySet()) {
            if (entry.getKey() instanceof Number id && entry.getValue() instanceof StringRepresentable name) {
                result.put(name.asString(), id.intValue());
            }
        }

        return result;
    }

    @Override
    public void removeItems(@NotNull IntSet itemIds) throws Exception {
        var source = YamlFormat.DEFAULT.load(this.filepath);

        itemIds.forEach(id -> source.set(id, null));

        YamlFormat.DEFAULT.save(source, this.filepath);
    }

    @Override
    public void renameItems(@NotNull Int2ObjectMap<String> idToNewNameMap) throws Exception {
        var source = YamlFormat.DEFAULT.load(this.filepath);

        for (var entry : idToNewNameMap.int2ObjectEntrySet()) {
            if (source.get(entry.getIntKey()) != NullNode.NULL) {
                source.set(entry.getIntKey(), entry.getValue());
            } else {
                throw new IllegalStateException("Cannot rename item due to %s does not exist. New name: %s".formatted(entry.getIntKey(), entry.getValue()));
            }
        }

        YamlFormat.DEFAULT.save(source, this.filepath);
    }

    public @NotNull Path filepath() {
        return this.filepath;
    }

    static void appendNewItem(@NotNull BufferedWriter writer, int id, @NotNull String name) throws IOException {
        writer.write(Integer.toString(id));
        writer.append(':').append(' ');
        writer.append('"').append(name).append('"'); // TODO: invalid item name check
        writer.newLine();
    }
}
