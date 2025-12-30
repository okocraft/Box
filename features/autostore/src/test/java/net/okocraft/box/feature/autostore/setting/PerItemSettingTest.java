package net.okocraft.box.feature.autostore.setting;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.okocraft.box.test.shared.model.item.DummyItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PerItemSettingTest {

    private static final DummyItem ITEM_1 = new DummyItem(1, "dummy_item_1");
    private static final DummyItem ITEM_2 = new DummyItem(2, "dummy_item_2");

    @Test
    void testEnabled() {
        PerItemSetting setting = new PerItemSetting();

        // Initially, isEnabled returns false for all items.
        Assertions.assertFalse(setting.isEnabled(ITEM_1));
        Assertions.assertEquals(IntSet.of(), setting.getEnabledItems());

        // Enable "dummy_item_1"
        setting.setEnabled(ITEM_1, true);
        Assertions.assertTrue(setting.isEnabled(ITEM_1));
        Assertions.assertEquals(IntSet.of(ITEM_1.getInternalId()), setting.getEnabledItems());

        // Disable "dummy_item_1"
        setting.setEnabled(ITEM_1, false);
        Assertions.assertFalse(setting.isEnabled(ITEM_1));
        Assertions.assertEquals(IntSet.of(), setting.getEnabledItems());
    }

    @Test
    void testToggle() {
        PerItemSetting setting = new PerItemSetting();

        // Initially, isEnabled returns false for all items.
        Assertions.assertFalse(setting.isEnabled(ITEM_1));
        Assertions.assertEquals(IntSet.of(), setting.getEnabledItems());

        // Enable "dummy_item_1"
        setting.toggleEnabled(ITEM_1);
        Assertions.assertTrue(setting.isEnabled(ITEM_1));
        Assertions.assertEquals(IntSet.of(ITEM_1.getInternalId()), setting.getEnabledItems());

        // Disable "dummy_item_1"
        setting.toggleEnabled(ITEM_1);
        Assertions.assertFalse(setting.isEnabled(ITEM_1));
        Assertions.assertEquals(IntSet.of(), setting.getEnabledItems());
    }

    @Test
    void testClearAndEnableItems() {
        PerItemSetting setting = new PerItemSetting();

        // First, enable "dummy_item_1"
        setting.setEnabled(ITEM_1, true);
        Assertions.assertTrue(setting.isEnabled(ITEM_1));
        Assertions.assertFalse(setting.isEnabled(ITEM_2));
        Assertions.assertEquals(IntSet.of(ITEM_1.getInternalId()), setting.getEnabledItems());

        // Second, clear and enable "dummy_item_2" ("dummy_item_1" should be disabled)
        setting.clearAndEnableItems(IntList.of(ITEM_2.getInternalId()));
        Assertions.assertFalse(setting.isEnabled(ITEM_1));
        Assertions.assertTrue(setting.isEnabled(ITEM_2));
        Assertions.assertEquals(IntSet.of(ITEM_2.getInternalId()), setting.getEnabledItems());
    }
}
