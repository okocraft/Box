package net.okocraft.box.core.model.manager.customdata;

import com.github.siroshun09.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.model.customdata.CustomDataManager;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;

public class BoxCustomDataManager implements CustomDataManager {

    private final CustomDataStorage storage;

    public BoxCustomDataManager(@NotNull CustomDataStorage storage) {
        this.storage = storage;
    }

    @Override
    public @NotNull MapNode loadData(@NotNull Key key) throws Exception {
        return this.storage.loadData(Objects.requireNonNull(key));
    }

    @Override
    public void saveData(@NotNull Key key, @NotNull MapNode mapNode) throws Exception {
        this.storage.saveData(Objects.requireNonNull(key), Objects.requireNonNull(mapNode));
    }

    @Override
    public void visitData(@NotNull String namespace, @NotNull BiConsumer<Key, MapNode> consumer) throws Exception {
        if (Key.checkNamespace(namespace).isPresent()) {
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        }

        this.storage.visitData(namespace, Objects.requireNonNull(consumer));
    }
}
