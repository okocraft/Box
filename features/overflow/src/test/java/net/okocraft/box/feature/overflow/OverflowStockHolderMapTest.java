package net.okocraft.box.feature.overflow;

import dev.siroshun.configapi.core.node.MapNode;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.model.customdata.CustomDataManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

class OverflowStockHolderMapTest {

    @Test
    void testCreateAndReloadMapping() throws Exception {
        MemoryCustomDataManager customDataManager = new MemoryCustomDataManager();
        OverflowStockHolderMap holderMap = new OverflowStockHolderMap(customDataManager);
        UUID sourceUuid = UUID.randomUUID();

        UUID nextUuid = holderMap.getOrCreateNext(sourceUuid);

        Assertions.assertEquals(nextUuid, holderMap.getOrCreateNext(sourceUuid));
        Assertions.assertTrue(holderMap.isOverflowHolder(nextUuid));
        Assertions.assertFalse(holderMap.isOverflowHolder(sourceUuid));

        OverflowStockHolderMap reloaded = new OverflowStockHolderMap(customDataManager);

        Assertions.assertEquals(nextUuid, reloaded.getOrCreateNext(sourceUuid));
        Assertions.assertTrue(reloaded.isOverflowHolder(nextUuid));
    }

    private static final class MemoryCustomDataManager implements CustomDataManager {

        private final Map<Key, MapNode> data = new HashMap<>();

        @Override
        public @NotNull MapNode loadData(@NotNull Key key) {
            MapNode node = this.data.get(key);
            return node != null ? node.copy() : MapNode.create();
        }

        @Override
        public void saveData(@NotNull Key key, @NotNull MapNode mapNode) {
            this.data.put(key, mapNode.copy());
        }

        @Override
        public void visitData(@NotNull String namespace, @NotNull BiConsumer<Key, MapNode> consumer) {
            this.data.forEach((key, node) -> {
                if (key.namespace().equals(namespace)) {
                    consumer.accept(key, node.copy());
                }
            });
        }
    }
}
