package net.okocraft.box.core.model.manager.item;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

class BoxItemMapTest {

    private static final TestItem TEST_ITEM = new TestItem(100, "test_item");
    private static final String RENAMED_ITEM_NAME = "renamed_item";

    @Test
    void testLock() throws ExecutionException, InterruptedException {
        var itemMap = createItemMap();
        Boolean[] states = new Boolean[3];

        var lockingExecutor = Executors.newSingleThreadExecutor();
        var readingExecutor = Executors.newSingleThreadExecutor();

        readingExecutor.submit(() -> states[0] = !itemMap.isRegistered(TEST_ITEM.getPlainName())).get();

        Assertions.assertTrue(states[0]);

        lockingExecutor.submit(itemMap::acquireWriteLock).get();

        var readingFuture = readingExecutor.submit(() -> states[1] = itemMap.isRegistered(TEST_ITEM.getPlainName()));

        Assertions.assertNull(states[1]);

        lockingExecutor.submit(() -> {
            itemMap.addItemAtUnsynchronized(TEST_ITEM);
            states[2] = true;
            itemMap.releaseWriteLock();
        }).get();

        readingFuture.get();

        Assertions.assertTrue(states[1]);
        Assertions.assertTrue(states[2]);
    }

    @Test
    void testRegisterItem() {
        var itemMap = createItemMap();

        Assertions.assertFalse(itemMap.isRegistered(TEST_ITEM.getPlainName()));
        Assertions.assertNull(itemMap.getByItemName(TEST_ITEM.getPlainName()));
        Assertions.assertNull(itemMap.getById(TEST_ITEM.getInternalId()));
        Assertions.assertTrue(itemMap.getItemNameList().isEmpty());
        Assertions.assertTrue(itemMap.getItemList().isEmpty());

        itemMap.acquireWriteLock();

        try { // See BoxItemManager#registerNewCustomItem
            Assertions.assertFalse(itemMap.checkItemNameAtUnsynchronized(TEST_ITEM.getPlainName()));
            // Assertions.assertFalse(itemMap.checkItemAtUnsynchronized(base.itemStack)) // We cannot use ItemStack

            itemMap.addItemAtUnsynchronized(TEST_ITEM);
            itemMap.rebuildCache();
        } finally {
            itemMap.releaseWriteLock();
        }

        Assertions.assertTrue(itemMap.isRegistered(TEST_ITEM.getPlainName()));
        Assertions.assertSame(TEST_ITEM, itemMap.getByItemName(TEST_ITEM.getPlainName()));
        Assertions.assertSame(TEST_ITEM, itemMap.getById(TEST_ITEM.getInternalId()));
        Assertions.assertEquals(TEST_ITEM.getPlainName(), itemMap.getItemNameList().get(0));
        Assertions.assertSame(TEST_ITEM, itemMap.getItemList().get(0));
    }

    @Test
    void testRenameItem() {
        var itemMap = createItemMap();

        itemMap.addItemAtUnsynchronized(TEST_ITEM);
        itemMap.rebuildCache();

        Assertions.assertTrue(itemMap.isRegistered(TEST_ITEM.getPlainName()));
        Assertions.assertSame(TEST_ITEM, itemMap.getByItemName(TEST_ITEM.getPlainName()));
        Assertions.assertSame(TEST_ITEM, itemMap.getById(TEST_ITEM.getInternalId()));
        Assertions.assertEquals(TEST_ITEM.getPlainName(), itemMap.getItemNameList().get(0));
        Assertions.assertSame(TEST_ITEM, itemMap.getItemList().get(0));

        itemMap.acquireWriteLock();
        TestItem renamedItem;

        try { // See BoxItemManager#renameItem
            Assertions.assertFalse(itemMap.checkItemNameAtUnsynchronized(RENAMED_ITEM_NAME));

            itemMap.removeItemAtUnsynchronized(TEST_ITEM);

            renamedItem = TEST_ITEM.rename(RENAMED_ITEM_NAME);

            itemMap.addItemAtUnsynchronized(renamedItem);
            itemMap.rebuildCache();
        } finally {
            itemMap.releaseWriteLock();
        }

        Assertions.assertFalse(itemMap.isRegistered(TEST_ITEM.getPlainName()));
        Assertions.assertTrue(itemMap.isRegistered(RENAMED_ITEM_NAME));
        Assertions.assertNull(itemMap.getByItemName(TEST_ITEM.getPlainName()));
        Assertions.assertSame(renamedItem, itemMap.getByItemName(RENAMED_ITEM_NAME));
        Assertions.assertSame(renamedItem, itemMap.getById(TEST_ITEM.getInternalId()));
        Assertions.assertEquals(RENAMED_ITEM_NAME, itemMap.getItemNameList().get(0));
        Assertions.assertSame(renamedItem, itemMap.getItemList().get(0));
    }

    private @NotNull BoxItemMap createItemMap() {
        return new BoxItemMap();
    }

    private record TestItem(int internalId, @NotNull String plainName) implements BoxItem {

        @Override
        public int getInternalId() {
            return internalId;
        }

        @Override
        public @NotNull String getPlainName() {
            return plainName;
        }

        @Override
        public @NotNull ItemStack getOriginal() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull ItemStack getClonedItem() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull Component getDisplayName() {
            throw new UnsupportedOperationException();
        }

        @Contract("_ -> new")
        public @NotNull TestItem rename(@NotNull String newName) {
            return new TestItem(internalId, newName);
        }
    }
}
