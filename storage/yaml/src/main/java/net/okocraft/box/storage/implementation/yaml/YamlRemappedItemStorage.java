package net.okocraft.box.storage.implementation.yaml;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.RemappedItemStorage;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class YamlRemappedItemStorage implements RemappedItemStorage {

    private final Path filepath;

    YamlRemappedItemStorage(@NotNull Path rootDirectory) {
        this.filepath = rootDirectory.resolve("items").resolve("remapped-items.yml");
    }

    @Override
    public @NotNull Map<MCDataVersion, Int2IntMap> loadRemappedIds() throws Exception {
        var source = YamlFormat.DEFAULT.load(this.filepath);
        var result = new Object2ObjectOpenHashMap<MCDataVersion, Int2IntMap>();

        for (var entry : source.value().entrySet()) {
            if (entry.getKey() instanceof Number oldId && entry.getValue() instanceof MapNode section) {
                result.computeIfAbsent(
                        MCDataVersion.of(section.getInteger("in-version")),
                        ignored -> new Int2IntOpenHashMap()
                ).put(oldId.intValue(), section.getInteger("remapped-to"));
            }
        }

        return result;
    }

    @Override
    public @NotNull Int2IntMap loadRemappedIds(@NotNull MCDataVersion version) throws Exception {
        var source = YamlFormat.DEFAULT.load(this.filepath);
        var result = new Int2IntOpenHashMap();

        for (var entry : source.value().entrySet()) {
            if (entry.getKey() instanceof Number oldId && entry.getValue() instanceof MapNode section) {
                if (section.getInteger("in-version") == version.dataVersion()) {
                    result.put(oldId.intValue(), section.getInteger("remapped-to"));
                }
            }
        }

        return result;
    }

    @Override
    public void saveRemappedItem(int id, @NotNull String name, int remappedTo, @NotNull MCDataVersion inVersion) throws Exception {
        Files.createDirectories(this.filepath.getParent());
        try (var writer = Files.newBufferedWriter(this.filepath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            writer.write(Integer.toString(id));
            writer.write(": ");
            writer.newLine();
            writer.append("  name: ").append(name);
            writer.newLine();
            writer.append("  remapped-to: ").append(Integer.toString(remappedTo));
            writer.newLine();
            writer.append("  in-version: ").append(Integer.toString(inVersion.dataVersion()));
            writer.newLine();
        }
    }
}
