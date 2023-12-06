package net.okocraft.box.core.config;

import com.github.siroshun09.configapi.core.comment.SimpleComment;
import com.github.siroshun09.configapi.core.node.CommentableNode;
import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.node.StringValue;
import com.github.siroshun09.configapi.core.serialization.key.KeyGenerator;
import com.github.siroshun09.configapi.core.serialization.record.RecordDeserializer;
import com.github.siroshun09.configapi.core.serialization.record.RecordSerializer;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.registry.BaseStorageContext;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("UnstableApiUsage")
public class Config {

    public static final String FILENAME = "config.yml";

    public static @NotNull Config inDirectory(@NotNull Path directory) {
        return new Config(directory.resolve(FILENAME));
    }

    private final Path filepath;
    private final AtomicReference<CoreSetting> coreSettingRef = new AtomicReference<>();

    private Config(@NotNull Path filepath) {
        this.filepath = filepath;
    }

    public @NotNull Storage loadAndCreateStorage(@NotNull StorageRegistry storageRegistry, @NotNull BaseStorageContext context) throws IOException {
        var loaded = YamlFormat.COMMENT_PROCESSING.load(this.filepath);

        this.loadCoreSetting(loaded, true);
        var storage = this.createStorageFromSection(loaded, storageRegistry, context);

        YamlFormat.COMMENT_PROCESSING.save(loaded, this.filepath);

        return storage;
    }

    public void reload() throws IOException {
        this.loadCoreSetting(YamlFormat.DEFAULT.load(this.filepath), false);
    }

    public @NotNull CoreSetting coreSetting() {
        return Objects.requireNonNull(this.coreSettingRef.get());
    }

    public @NotNull Path filepath() {
        return this.filepath;
    }

    private void loadCoreSetting(@NotNull MapNode source, boolean applyDefaults) {
        var coreSection = source.getOrCreateMap("core");
        var coreDeserializer = RecordDeserializer.create(CoreSetting.class, KeyGenerator.CAMEL_TO_KEBAB);

        if (applyDefaults) {
            if (!coreSection.hasComment()) {
                coreSection.setComment(SimpleComment.create("\nThe core settings of Box.\n\n"));
            }

            var serializer = RecordSerializer.create(KeyGenerator.CAMEL_TO_KEBAB);
            applyDefaults(serializer.serializeDefault(CoreSetting.class), coreSection);
        }

        this.coreSettingRef.set(coreDeserializer.deserialize(coreSection));
    }

    private @NotNull Storage createStorageFromSection(@NotNull MapNode source, @NotNull StorageRegistry registry, @NotNull BaseStorageContext context) {
        var storageSection = source.getOrCreateMap("storage");

        if (!storageSection.hasComment()) {
            storageSection.setComment(SimpleComment.create("\nThe settings of storage that will be used for loading/saving data.\n\n"));
        }

        var storageType = storageSection.getString("type").toLowerCase(Locale.ENGLISH);

        if (storageType.isEmpty()) {
            storageType = registry.getDefaultStorageName();
            storageSection.set("type", CommentableNode.withComment(new StringValue(storageType), SimpleComment.create("The type of storage to be used.", "inline")));
        }

        Storage storage = null;
        var serializer = RecordSerializer.create(KeyGenerator.CAMEL_TO_KEBAB);

        for (var type : registry.getEntries()) {
            var name = type.name().toLowerCase(Locale.ENGLISH);
            var defaultSetting = serializer.serializeDefault(type.settingClass());

            MapNode section;

            if (defaultSetting.value().isEmpty()) { // There is no setting for this storage
                section = MapNode.empty();
            } else {
                section = storageSection.getOrCreateMap(name);
                applyDefaults(defaultSetting, section);
            }

            if (storageType.equals(name)) {
                storage = type.create(context, section);
            }
        }

        if (storage == null) {
            var defaultEntry = registry.getDefault();
            context.logger().warning("The storage type '" + storageType + "' not found.");
            context.logger().warning("Using the default storage type... (" + defaultEntry.name() + ")");
            storage = defaultEntry.create(context, storageSection.getMap(defaultEntry.name().toLowerCase(Locale.ENGLISH)));
        }

        return storage;
    }

    private static void applyDefaults(@NotNull MapNode source, @NotNull MapNode target) {
        for (var defaultEntry : source.value().entrySet()) {
            target.setIfAbsent(defaultEntry.getKey(), defaultEntry.getValue());
        }
    }
}
