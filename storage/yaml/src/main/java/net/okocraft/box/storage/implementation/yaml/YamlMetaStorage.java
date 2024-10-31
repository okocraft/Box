package net.okocraft.box.storage.implementation.yaml;

import dev.siroshun.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.version.StorageVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;

class YamlMetaStorage {

    private final Path lastItemIdFilepath;
    private final Path dataVersionFilepath;
    private final Path storageVersionFilepath;
    private final AtomicInteger lastItemId = new AtomicInteger();

    private @Nullable MCDataVersion dataVersion;
    private StorageVersion storageVersion;

    YamlMetaStorage(@NotNull Path rootDirectory) {
        var directory = rootDirectory.resolve("meta");
        this.lastItemIdFilepath = directory.resolve("last-item-id.dat");
        this.dataVersionFilepath = directory.resolve("data-version.dat");
        this.storageVersionFilepath = directory.resolve("storage-version.dat");
    }

    void migrateFromOldMetaFile(@NotNull Path filepath) throws IOException {
        var source = YamlFormat.DEFAULT.load(filepath);

        saveInt(this.lastItemIdFilepath, source.getInteger("last-used-item-id"));
        saveInt(this.dataVersionFilepath, source.getInteger("data-version"));

        Files.move(filepath, YamlStoragePatches.createBackupFilepath(filepath));
    }

    void load() throws IOException {
        this.loadAsInt(this.lastItemIdFilepath).ifPresent(this.lastItemId::set);
        this.dataVersion = this.loadAsInt(this.dataVersionFilepath).stream().mapToObj(MCDataVersion::new).findFirst().orElse(null);
        this.storageVersion = this.loadAsInt(this.storageVersionFilepath).stream().mapToObj(StorageVersion::new).findFirst().orElse(StorageVersion.BEFORE_V6);
    }

    int newItemId() throws IOException {
        int id = this.lastItemId.incrementAndGet();
        saveInt(this.lastItemIdFilepath, id);
        return id;
    }

    int newItemIdWithoutSaving() {
        return this.lastItemId.incrementAndGet();
    }

    void saveLastItemId() throws IOException {
        saveInt(this.lastItemIdFilepath, this.lastItemId.get());
    }


    @Nullable
    MCDataVersion dataVersion() {
        return this.dataVersion;
    }

    void dataVersion(@NotNull MCDataVersion dataVersion) throws IOException {
        this.dataVersion = dataVersion;
        saveInt(this.dataVersionFilepath, dataVersion.dataVersion());
    }

    @NotNull
    StorageVersion storageVersion() {
        return this.storageVersion;
    }

    void storageVersion(@NotNull StorageVersion storageVersion) throws IOException {
        this.storageVersion = storageVersion;
        saveInt(this.storageVersionFilepath, storageVersion.value());
    }

    private @NotNull OptionalInt loadAsInt(@NotNull Path filepath) throws IOException {
        if (Files.notExists(filepath)) {
            return OptionalInt.empty();
        }

        var content = Files.readString(filepath, StandardCharsets.UTF_8);

        try {
            return OptionalInt.of(Integer.parseInt(content));
        } catch (NumberFormatException e) {
            BoxLogger.logger().error("Read invalid value ({}) from {}.", content, filepath);
            return OptionalInt.empty();
        }
    }

    private static void saveInt(@NotNull Path filepath, int id) throws IOException {
        Files.createDirectories(filepath.getParent());
        Files.writeString(filepath, Integer.toString(id), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
