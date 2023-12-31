package net.okocraft.box.api.model.item;

import net.okocraft.box.api.util.AbstractVersionTest;
import org.junit.jupiter.api.Test;

class ItemVersionTest extends AbstractVersionTest<ItemVersion> {
    @Test
    void testComparing() {
        this.check(ItemVersion.of(1, 0), ItemVersion.of(2, 0), -1);
        this.check(ItemVersion.of(1, 0), ItemVersion.of(1, 1), -1);

        this.check(ItemVersion.of(1, 0), ItemVersion.of(1, 0), 0);

        this.check(ItemVersion.of(2, 0), ItemVersion.of(1, 0), 1);
        this.check(ItemVersion.of(1, 1), ItemVersion.of(1, 0), 1);
    }
}
