package net.okocraft.box.test.shared.storage.test;

import net.okocraft.box.storage.api.model.item.CustomItemStorage;
import net.okocraft.box.storage.api.model.item.ItemData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public abstract class CustomItemStorageTest<S> extends AbstractStorageTest<S> {

    private static final byte[] DATA = {(byte) 1, (byte) 2, (byte) 3};
    private static final byte[] UPDATED_DATA = {(byte) 1, (byte) 2, (byte) 3};

    @Test
    void testEmpty() throws Exception {
        var storage = this.newStorage();
        var customItemStorage = this.newCustomItemStorage(storage);

        try {
            customItemStorage.loadItemData(data -> Assertions.fail("Expected no custom items"));
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testAdd() throws Exception {
        var storage = this.newStorage();
        var customItemStorage = this.newCustomItemStorage(storage);

        try {
            int id = customItemStorage.newCustomItem("test", DATA);
            var loaded = new AtomicBoolean(false);
            customItemStorage.loadItemData(data -> {
                if (loaded.compareAndSet(false, true)) {
                    Assertions.assertEquals(id, data.internalId());
                    Assertions.assertEquals("test", data.plainName());
                    Assertions.assertArrayEquals(DATA, data.itemData());
                } else {
                    Assertions.fail("Custom item is loaded twice (add)");
                }
            });
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testUpdate() throws Exception {
        var storage = this.newStorage();
        var customItemStorage = this.newCustomItemStorage(storage);

        try {
            int id = customItemStorage.newCustomItem("test", DATA);
            customItemStorage.updateItemData(Stream.of(new ItemData(id, "test", UPDATED_DATA)));

            var loaded = new AtomicBoolean(false);
            customItemStorage.loadItemData(data -> {
                if (loaded.compareAndSet(false, true)) {
                    Assertions.assertEquals(id, data.internalId());
                    Assertions.assertEquals("test", data.plainName());
                    Assertions.assertArrayEquals(UPDATED_DATA, data.itemData());
                } else {
                    Assertions.fail("Custom item is loaded twice (update)");
                }
            });
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testRename() throws Exception {
        var storage = this.newStorage();
        var customItemStorage = this.newCustomItemStorage(storage);

        try {
            int id = customItemStorage.newCustomItem("test", DATA);
            customItemStorage.renameCustomItem(id, "renamed_test");

            var loaded = new AtomicBoolean(false);
            customItemStorage.loadItemData(data -> {
                if (loaded.compareAndSet(false, true)) {
                    Assertions.assertEquals(id, data.internalId());
                    Assertions.assertEquals("renamed_test", data.plainName());
                    Assertions.assertArrayEquals(UPDATED_DATA, data.itemData());
                } else {
                    Assertions.fail("Custom item is loaded twice (rename)");
                }
            });
        } finally {
            this.closeStorage(storage);
        }
    }

    protected abstract @NotNull CustomItemStorage newCustomItemStorage(@NotNull S storage) throws Exception;

}
