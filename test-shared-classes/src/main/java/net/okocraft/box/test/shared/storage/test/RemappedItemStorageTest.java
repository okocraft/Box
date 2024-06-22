package net.okocraft.box.test.shared.storage.test;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.RemappedItemStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public abstract class RemappedItemStorageTest<S> extends AbstractStorageTest<S> {

    @Test
    void testEmpty() throws Exception {
        var storage = this.newStorage();
        var remappedItemStorage = this.newRemappedItemStorage(storage);

        try {
            Assertions.assertTrue(remappedItemStorage.loadRemappedIds().isEmpty());
            Assertions.assertTrue(remappedItemStorage.loadRemappedIds(MCDataVersion.MC_1_20_5).isEmpty());
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testSaveAndLoad() throws Exception {
        var storage = this.newStorage();
        var remappedItemStorage = this.newRemappedItemStorage(storage);

        try {
            remappedItemStorage.saveRemappedItem(1, "test_1", 11, MCDataVersion.MC_1_20_5);
            remappedItemStorage.saveRemappedItem(2, "test_2", 22, MCDataVersion.MC_1_20_6);

            var v1_20_5 = new Int2IntArrayMap(new int[]{1}, new int[]{11});
            var v1_20_6 = new Int2IntArrayMap(new int[]{2}, new int[]{22});

            Assertions.assertEquals(Map.of(MCDataVersion.MC_1_20_5, v1_20_5, MCDataVersion.MC_1_20_6, v1_20_6), remappedItemStorage.loadRemappedIds());

            Assertions.assertEquals(v1_20_5, remappedItemStorage.loadRemappedIds(MCDataVersion.MC_1_20_5));
            Assertions.assertEquals(v1_20_6, remappedItemStorage.loadRemappedIds(MCDataVersion.MC_1_20_6));
        } finally {
            this.closeStorage(storage);
        }
    }

    protected abstract @NotNull RemappedItemStorage newRemappedItemStorage(@NotNull S storage) throws Exception;

}
