package net.okocraft.box.bundle;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.item.provider.DefaultItemProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VersionedImplsTest {
    @Test
    void testMC_1_20_5() {
        VersionedImpls impls = VersionedImpls.load(this.getClass().getClassLoader());
        DefaultItemProvider provider = impls.createDefaultItemProvider(MCDataVersion.MC_1_21_3);
        assertEquals(MCDataVersion.MC_1_21_3, provider.version());
    }
}
