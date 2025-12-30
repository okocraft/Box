package net.okocraft.box.feature.autostore.setting;

import net.okocraft.box.test.shared.model.item.DummyItem;
import net.okocraft.box.test.shared.util.UUIDFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AutoStoreSettingTest {

    private static final DummyItem ITEM = new DummyItem(1, "dummy_item");

    @Test
    void testShouldAutostore() {
        AutoStoreSetting setting = new AutoStoreSetting(UUIDFactory.byName("AutoStoreSettingTest"));

        // Initially, the item should autostore because the mode is all-mode.
        Assertions.assertTrue(setting.shouldAutoStore(ITEM));

        // Set to per-item mode.
        setting.setAllMode(false);
        Assertions.assertFalse(setting.shouldAutoStore(ITEM));

        // Enable the item in the per item setting.
        setting.getPerItemModeSetting().setEnabled(ITEM, true);
        Assertions.assertTrue(setting.shouldAutoStore(ITEM));
    }
}
