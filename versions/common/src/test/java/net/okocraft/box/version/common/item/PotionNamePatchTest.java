package net.okocraft.box.version.common.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class PotionNamePatchTest {
    @ParameterizedTest
    @CsvFileSource(resources = "potion_names.csv")
    void test(String legacy, String expected) {
        Assertions.assertEquals(expected, LegacyVersionPatches.potionName(legacy));
    }
}
