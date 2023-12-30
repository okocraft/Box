package net.okocraft.box.api.model.item;

import net.okocraft.box.api.util.MCDataVersion;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ItemVersionTest {

    @Test
    void testComparing() {
        check(ver(1, 0), ver(2, 0), -1);
        check(ver(1, 0), ver(1, 1), -1);

        check(ver(1, 0), ver(1, 0), 0);

        check(ver(2, 0), ver(1, 0), 1);
        check(ver(1, 1), ver(1, 0), 1);
    }

    private static void check(@NotNull ItemVersion v1, @NotNull ItemVersion v2, int expected) {
        Assertions.assertEquals(expected, v1.compareTo(v2));
        Assertions.assertEquals(expected == 1, v1.isAfter(v2));
        Assertions.assertEquals(expected == 0, v1.isSame(v2));
        Assertions.assertEquals(expected == -1, v1.isBefore(v2));
    }

    private static @NotNull ItemVersion ver(int dataVersion, int defaultItemVersion) {
        return new ItemVersion(new MCDataVersion(dataVersion), defaultItemVersion);
    }
}
