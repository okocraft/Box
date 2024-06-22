package net.okocraft.box.bundle;

import net.okocraft.box.api.util.MCDataVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionedImplsTest {
    @Test
    void testMC_1_20_5() {
        var impls = VersionedImpls.load(this.getClass().getClassLoader());
        var provider = impls.createDefaultItemProvider(MCDataVersion.MC_1_20_5);
        assertEquals(MCDataVersion.MC_1_20_5, provider.version());
    }
}
