package net.okocraft.box.api.util;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

public abstract class AbstractVersionTest<V extends Version<V>> {
    protected void check(@NotNull V v1, @NotNull V v2, int expected) {
        Assertions.assertEquals(expected, v1.compareTo(v2));
        Assertions.assertEquals(expected == 1, v1.isAfter(v2));
        Assertions.assertEquals(expected == 0, v1.isSame(v2));
        Assertions.assertEquals(expected == -1, v1.isBefore(v2));
        Assertions.assertEquals(expected == 1 || expected == 0, v1.isAfterOrSame(v2));
        Assertions.assertEquals(expected == -1 || expected == 0, v1.isBeforeOrSame(v2));
    }
}
