package net.okocraft.box.api.model.customdata;

import com.github.siroshun09.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public interface CustomDataManager {

    @NotNull MapNode loadData(@NotNull Key key) throws Exception;

    void saveData(@NotNull Key key, @NotNull MapNode mapNode) throws Exception;

    void visitData(@NotNull String namespace, @NotNull BiConsumer<Key, MapNode> consumer) throws Exception;

}
