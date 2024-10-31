package net.okocraft.box.storage.api.model.data;

import dev.siroshun.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public interface CustomDataStorage {

    default void updateFormatIfNeeded() throws Exception {
    }

    @NotNull MapNode loadData(@NotNull Key key) throws Exception;

    void saveData(@NotNull Key key, @NotNull MapNode mapNode) throws Exception;

    void visitData(@NotNull @KeyPattern.Namespace String namespace, @NotNull BiConsumer<Key, MapNode> consumer) throws Exception;

    void visitAllData(@NotNull BiConsumer<Key, MapNode> consumer) throws Exception;
}
