package net.okocraft.box.api.model.customdata;

import dev.siroshun.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * An interface to load/save custom data using {@link MapNode}.
 */
public interface CustomDataManager {

    /**
     * Loads the {@link MapNode}.
     *
     * @param key the {@link Key}
     * @return the saved {@link MapNode} or newly created {@link MapNode} if not exists
     * @throws Exception if a storage error occurred
     */
    @NotNull MapNode loadData(@NotNull Key key) throws Exception;

    /**
     * Saves the {@link MapNode}.
     *
     * @param key     the {@link Key}
     * @param mapNode the {@link MapNode} to save
     * @throws Exception if a storage error occurred
     */
    void saveData(@NotNull Key key, @NotNull MapNode mapNode) throws Exception;

    /**
     * Visits all {@link MapNode}s that are keyed with the specified namespace.
     *
     * @param namespace the namespace to get {@link MapNode}
     * @param consumer  the {@link BiConsumer} to consume loaded {@link MapNode} with its {@link Key}
     * @throws Exception                if a storage error occurred
     * @throws IllegalArgumentException if the specified namespace is invalid (it is checked by {@link Key#checkNamespace(String)})
     */
    void visitData(@NotNull String namespace, @NotNull BiConsumer<Key, MapNode> consumer) throws Exception;

}
