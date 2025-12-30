package net.okocraft.box.core.model.manager.item;

import net.okocraft.box.test.shared.model.item.DummyItem;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class BoxItemMapTest {

    private static final DummyItem ITEM = new DummyItem(100, "test_item");
    private static final String RENAMED_ITEM_NAME = "renamed_item";

    @Test
    void testLock() throws ExecutionException, InterruptedException {
        BoxItemMap itemMap = this.createItemMap();
        Boolean[] states = new Boolean[3];

        ExecutorService lockingExecutor = Executors.newSingleThreadExecutor();
        ExecutorService readingExecutor = Executors.newSingleThreadExecutor();

        readingExecutor.submit(() -> states[0] = !itemMap.isRegistered(ITEM.getPlainName())).get();

        Assertions.assertTrue(states[0]);

        lockingExecutor.submit(itemMap::acquireWriteLock).get();

        Future<Boolean> readingFuture = readingExecutor.submit(() -> states[1] = itemMap.isRegistered(ITEM.getPlainName()));

        Assertions.assertNull(states[1]);

        lockingExecutor.submit(() -> {
            itemMap.addItemAtUnsynchronized(ITEM);
            states[2] = true;
            itemMap.releaseWriteLock();
        }).get();

        readingFuture.get();

        Assertions.assertTrue(states[1]);
        Assertions.assertTrue(states[2]);
    }

    @Test
    void testRegisterItem() {
        BoxItemMap itemMap = this.createItemMap();

        Assertions.assertFalse(itemMap.isRegistered(ITEM.getPlainName()));
        Assertions.assertNull(itemMap.getByItemName(ITEM.getPlainName()));
        Assertions.assertNull(itemMap.getById(ITEM.getInternalId()));
        Assertions.assertTrue(itemMap.getItemNameList().isEmpty());
        Assertions.assertTrue(itemMap.getItemList().isEmpty());

        itemMap.acquireWriteLock();

        try { // See BoxItemManager#registerNewCustomItem
            Assertions.assertFalse(itemMap.checkItemNameAtUnsynchronized(ITEM.getPlainName()));
            // Assertions.assertFalse(itemMap.checkItemAtUnsynchronized(base.itemStack)) // We cannot use ItemStack

            itemMap.addItemAtUnsynchronized(ITEM);
            itemMap.rebuildCache();
        } finally {
            itemMap.releaseWriteLock();
        }

        Assertions.assertTrue(itemMap.isRegistered(ITEM.getPlainName()));
        Assertions.assertSame(ITEM, itemMap.getByItemName(ITEM.getPlainName()));
        Assertions.assertSame(ITEM, itemMap.getById(ITEM.getInternalId()));
        Assertions.assertEquals(ITEM.getPlainName(), itemMap.getItemNameList().get(0));
        Assertions.assertSame(ITEM, itemMap.getItemList().get(0));
    }

    @Test
    void testRenameItem() {
        BoxItemMap itemMap = this.createItemMap();

        itemMap.addItemAtUnsynchronized(ITEM);
        itemMap.rebuildCache();

        Assertions.assertTrue(itemMap.isRegistered(ITEM.getPlainName()));
        Assertions.assertSame(ITEM, itemMap.getByItemName(ITEM.getPlainName()));
        Assertions.assertSame(ITEM, itemMap.getById(ITEM.getInternalId()));
        Assertions.assertEquals(ITEM.getPlainName(), itemMap.getItemNameList().get(0));
        Assertions.assertSame(ITEM, itemMap.getItemList().get(0));

        itemMap.acquireWriteLock();
        DummyItem renamedItem;

        try { // See BoxItemManager#renameItem
            Assertions.assertFalse(itemMap.checkItemNameAtUnsynchronized(RENAMED_ITEM_NAME));

            itemMap.removeItemAtUnsynchronized(ITEM);

            renamedItem = ITEM.rename(RENAMED_ITEM_NAME);

            itemMap.addItemAtUnsynchronized(renamedItem);
            itemMap.rebuildCache();
        } finally {
            itemMap.releaseWriteLock();
        }

        Assertions.assertFalse(itemMap.isRegistered(ITEM.getPlainName()));
        Assertions.assertTrue(itemMap.isRegistered(RENAMED_ITEM_NAME));
        Assertions.assertNull(itemMap.getByItemName(ITEM.getPlainName()));
        Assertions.assertSame(renamedItem, itemMap.getByItemName(RENAMED_ITEM_NAME));
        Assertions.assertSame(renamedItem, itemMap.getById(ITEM.getInternalId()));
        Assertions.assertEquals(RENAMED_ITEM_NAME, itemMap.getItemNameList().get(0));
        Assertions.assertSame(renamedItem, itemMap.getItemList().get(0));
    }

    private @NotNull BoxItemMap createItemMap() {
        return new BoxItemMap();
    }
}
