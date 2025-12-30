package net.okocraft.box.storage.implementation.yaml;

import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.util.BoxLogger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static net.okocraft.box.api.util.BoxLogger.logger;

final class YamlStoragePatches {

    static void v6(@NotNull YamlStorage storage) throws IOException {
        BoxLogger.logger().info("Updating {}...", storage.defaultItemStorage.filepath());
        migrateDefaultItems(storage.defaultItemStorage.filepath());
    }

    private static void migrateDefaultItems(@NotNull Path filepath) throws IOException {
        if (!Files.isRegularFile(filepath)) {
            return;
        }

        MapNode source = YamlFormat.DEFAULT.load(filepath);
        Files.move(filepath, createBackupFilepath(filepath));

        try (BufferedWriter writer = Files.newBufferedWriter(filepath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Object key : source.value().keySet()) {
                int id;
                if (key instanceof Number number) {
                    id = number.intValue();
                } else {
                    try {
                        id = Integer.parseInt(String.valueOf(key));
                    } catch (NumberFormatException e) {
                        logger().warn("Invalid item key: {}", key);
                        continue;
                    }
                }

                String name = source.getMap(key).getStringOrNull("name");

                if (name != null) {
                    YamlDefaultItemStorage.appendNewItem(writer, id, name);
                } else {
                    logger().warn("Cannot get the item name of the id '{}'", id);
                }
            }
        }
    }

    static @NotNull Path createBackupFilepath(@NotNull Path filepath) {
        return filepath.getParent().resolve(filepath.getFileName().toString() + ".back-" + System.currentTimeMillis());
    }

    private YamlStoragePatches() {
        throw new UnsupportedOperationException();
    }
}
