package net.okocraft.box.storage.api.model.data;

import com.github.siroshun09.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public interface CustomDataStorage {

    void init() throws Exception;

    default void updateFormatIfNeeded() throws Exception {
    }

    @NotNull MapNode loadData(@NotNull Key key) throws Exception;

    void saveData(@NotNull Key key, @NotNull MapNode mapNode) throws Exception;

    void visitData(@NotNull String namespace, @NotNull BiConsumer<Key, MapNode> consumer) throws Exception;

    void visitAllData(@NotNull BiConsumer<Key, MapNode> consumer) throws Exception;
}
