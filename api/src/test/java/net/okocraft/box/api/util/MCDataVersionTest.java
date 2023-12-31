package net.okocraft.box.api.util;

import org.junit.jupiter.api.Test;

class MCDataVersionTest extends AbstractVersionTest<MCDataVersion> {
    @Test
    void testComparing() {
        this.check(MCDataVersion.of(1), MCDataVersion.of(2), -1);
        this.check(MCDataVersion.of(1), MCDataVersion.of(1), 0);
        this.check(MCDataVersion.of(2), MCDataVersion.of(1), 1);
    }
}
