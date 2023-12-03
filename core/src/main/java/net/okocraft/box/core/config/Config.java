package net.okocraft.box.core.config;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.node.NullNode;
import com.github.siroshun09.configapi.core.serialization.key.KeyGenerator;
import com.github.siroshun09.configapi.core.serialization.record.RecordDeserializer;
import com.github.siroshun09.configapi.core.serialization.record.RecordSerializer;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import net.okocraft.box.storage.api.registry.BaseStorageContext;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("UnstableApiUsage")
public class Config {

    private final Path filepath;
    private final AtomicReference<CoreSetting> coreSettingRef = new AtomicReference<>();

    public Config(@NotNull Path filepath) {
        this.filepath = filepath;
    }

    public @NotNull Storage loadAndCreateStorage(@NotNull StorageRegistry storageRegistry, @NotNull BaseStorageContext context) throws IOException {
        MapNode loaded;

        try (var reader = Files.newBufferedReader(this.filepath, StandardCharsets.UTF_8)) {
            loaded = YamlFormat.COMMENT_PROCESSING.load(reader);
        }

        var serializer = RecordSerializer.create(KeyGenerator.CAMEL_TO_KEBAB);

        var coreSection = loaded.getOrCreateMap("core");
        var coreDeserializer = deserializer(CoreSetting.class);

        applyDefaults(serializer.serialize(coreDeserializer.deserialize(MapNode.empty())), coreSection);
        this.coreSettingRef.set(coreDeserializer.deserialize(coreSection));

        var storageSection = loaded.getOrCreateMap("storage");

        var storageType = storageSection.getString("type").toLowerCase(Locale.ENGLISH);

        if (storageType.isEmpty()) {
            storageSection.set("type", storageRegistry.getDefaultStorageName());
            storageType = storageRegistry.getDefaultStorageName();
        }

        Storage storage = null;

        for (var entry : storageRegistry.getEntries()) {
            var name = entry.name().toLowerCase(Locale.ENGLISH);
            var section = storageSection.getOrCreateMap(name);

            applyDefaults(
                    serializer.serialize(deserializer(entry.settingClass()).deserialize(MapNode.empty())),
                    section
            );

            if (storageType.equals(name)) {
                storage = entry.create(context, section);
            }
        }

        try (var writer = Files.newBufferedWriter(this.filepath, StandardCharsets.UTF_8)) {
            YamlFormat.COMMENT_PROCESSING.save(loaded, writer);
        }

        if (storage == null) {
            var defaultEntry = storageRegistry.getDefault();
            context.logger().warning("The storage type '" + storageType + "' not found.");
            context.logger().warning("Using the default storage type... (" + defaultEntry.name() + ")");
            storage = defaultEntry.create(context, storageSection.getMap(defaultEntry.name().toLowerCase(Locale.ENGLISH)));
        }

        return storage;
    }

    public void reload() throws IOException {
        try (var reader = Files.newBufferedReader(this.filepath, StandardCharsets.UTF_8)) {
            var loaded = YamlFormat.DEFAULT.load(reader);
            this.coreSettingRef.set(deserializer(CoreSetting.class).deserialize(loaded.getMap("core")));
        }
    }

    public @NotNull CoreSetting coreSetting() {
        return Objects.requireNonNull(this.coreSettingRef.get());
    }

    private void applyDefaults(@NotNull MapNode source, @NotNull MapNode target) {
        for (var defaultEntry : source.value().entrySet()) {
            if (target.get(defaultEntry.getKey()) == NullNode.NULL) { // setIfAbsent?
                target.set(defaultEntry.getKey(), defaultEntry.getValue());
            }
        }
    }

    private <R extends Record> @NotNull RecordDeserializer<R> deserializer(@NotNull Class<R> clazz) {
        return RecordDeserializer.create(clazz, KeyGenerator.CAMEL_TO_KEBAB);
    }
}
