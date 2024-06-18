package net.okocraft.box.storage.implementation.yaml;

import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.storage.api.model.item.ItemData;
import net.okocraft.box.storage.api.model.item.CustomItemStorage;
import net.okocraft.box.storage.api.util.SneakyThrow;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.stream.Stream;

class YamlCustomItemStorage implements CustomItemStorage {

    private final YamlMetaStorage metaStorage;
    private final Path filepath;

    YamlCustomItemStorage(@NotNull Path rootDirectory, @NotNull YamlMetaStorage metaStorage) {
        this.metaStorage = metaStorage;
        this.filepath = rootDirectory.resolve("items").resolve("custom-items.yml");
    }

    @Override
    public void loadItemData(@NotNull Consumer<ItemData> dataConsumer) throws Exception {
        if (!Files.isRegularFile(this.filepath)) {
            return;
        }

        var source = YamlFormat.DEFAULT.load(this.filepath);

        for (var key : source.value().keySet()) {
            if (!(key instanceof Number id)) {
                continue;
            }

            var section = source.getMap(key);
            var name = section.getStringOrNull("name");
            var data = section.getStringOrNull("data");

            if (name != null && data != null) {
                dataConsumer.accept(new ItemData(
                        id.intValue(),
                        name,
                        Base64.getDecoder().decode(data)
                ));
            }
        }
    }

    @Override
    public void updateItemData(@NotNull Stream<ItemData> items) throws Exception {
        try (var writer = Files.newBufferedWriter(this.filepath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            items.forEach(item -> {
                try {
                    appendNewItem(writer, item.internalId(), item.plainName(), item.itemData());
                } catch (IOException e) {
                    SneakyThrow.sneaky(e);
                }
            });
        }
    }

    @Override
    public int newCustomItem(@NotNull String name, byte[] data) throws Exception {
        int id = this.metaStorage.newItemId();

        var parent = this.filepath.getParent();
        if (!Files.isDirectory(parent)) {
            Files.createDirectories(parent);
        }

        try (var writer = Files.newBufferedWriter(this.filepath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            appendNewItem(writer, id, name, data);
        }

        return id;
    }

    @Override
    public void renameCustomItem(int id, @NotNull String newName) throws Exception {
        var mapNode = YamlFormat.DEFAULT.load(this.filepath);
        mapNode.getOrCreateMap(id).set("name", newName);
        YamlFormat.DEFAULT.save(mapNode, this.filepath);
    }

    private static void appendNewItem(@NotNull BufferedWriter writer, int id, @NotNull String name, byte[] itemData) throws IOException {
        writer.write(Integer.toString(id));
        writer.append(':');
        writer.newLine();
        writer.append("  name: \"").append(name).append('"'); // TODO: invalid item name check
        writer.newLine();
        writer.append("  data: ").append(Base64.getEncoder().encodeToString(itemData));
        writer.newLine();
    }
}
