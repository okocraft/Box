package net.okocraft.box.storage.api.registry;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.serialization.record.RecordDeserializer;
import net.okocraft.box.storage.api.model.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record StorageType<R extends Record>(@NotNull String name, @NotNull Class<R> settingClass,
                                            @NotNull Function<StorageContext<R>, Storage> storageFunction) {
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Storage create(@NotNull BaseStorageContext context, @NotNull MapNode section) {
        return this.storageFunction.apply(new StorageContext<>(context, RecordDeserializer.create(this.settingClass).deserialize(section)));
    }
}
