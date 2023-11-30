package net.okocraft.box.api.util;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ItemNameGeneratorTest {

    private static final Key MINECRAFT_KEY_1 = Key.key(Key.MINECRAFT_NAMESPACE, "key1");
    private static final Key MINECRAFT_KEY_2 = Key.key(Key.MINECRAFT_NAMESPACE, "key2");
    private static final Key CUSTOM_KEY = Key.key("custom", "key");
    private static final byte[] BYTES = new byte[]{0, 1, 2, 3, 4};
    private static final String SHA_1 = ItemNameGenerator.sha1(BYTES);

    @Test
    void testKey() {
        Assertions.assertEquals("KEY1", ItemNameGenerator.key(MINECRAFT_KEY_1));
        Assertions.assertEquals("CUSTOM_KEY", ItemNameGenerator.key(CUSTOM_KEY));
    }

    @Test
    void testKeys() {
        Assertions.assertEquals("KEY1_KEY2", ItemNameGenerator.keys(MINECRAFT_KEY_1, MINECRAFT_KEY_2));
        Assertions.assertEquals("KEY1_CUSTOM_KEY", ItemNameGenerator.keys(MINECRAFT_KEY_1, CUSTOM_KEY));
        Assertions.assertEquals("CUSTOM_KEY_KEY1", ItemNameGenerator.keys(CUSTOM_KEY, MINECRAFT_KEY_1));
        Assertions.assertEquals("KEY1_KEY2_CUSTOM_KEY", ItemNameGenerator.keys(MINECRAFT_KEY_1, MINECRAFT_KEY_2, CUSTOM_KEY));
    }

    @Test
    void testItemStack() {
        Assertions.assertEquals("KEY1_" + SHA_1, ItemNameGenerator.itemStack(MINECRAFT_KEY_1, BYTES));
    }
}
