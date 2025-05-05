package net.okocraft.box.storage.api.registry;

import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.serialization.record.RecordDeserializer;
import dev.siroshun.serialization.core.key.KeyGenerator;
import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.Function;

public record StorageType<R extends Record>(@NotNull String name, @NotNull Class<R> settingClass,
                                            @NotNull Function<StorageContext<R>, Storage> storageFunction) {
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Storage create(@NotNull Path pluginDirectory, @NotNull MapNode section) {
        return this.storageFunction.apply(new StorageContext<>(pluginDirectory, RecordDeserializer.create(this.settingClass, KeyGenerator.CAMEL_TO_KEBAB).deserialize(section)));
    }
}
