package net.okocraft.box.storage.implementation.yaml;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.okocraft.box.test.shared.storage.test.UserStorageTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

class YamlUserStorageTest {

    @Test
    void testUserMap() {
        var userMap = new YamlUserStorage.UserMap();

        var uuid = UserStorageTest.TEST_USER_1.getUUID();
        var name = UserStorageTest.TEST_USER_1.getName().orElseThrow();

        userMap.putUUIDAndUsername(uuid, name);
        Assertions.assertEquals(uuid, userMap.searchForUUID(name)); // obtain uuid
        Assertions.assertEquals(uuid, userMap.searchForUUID(name.toUpperCase(Locale.ENGLISH))); // obtain uuid, but uppercase name (should be case-insensitive)
        Assertions.assertNull(userMap.searchForUUID("unknown")); // unknown name
        Assertions.assertEquals(name, userMap.searchForUsername(uuid)); // obtain name (same as passed name, not lowercase)
        Assertions.assertNull(userMap.searchForUsername(UUID.randomUUID())); // unknown uuid

        userMap.putUUIDAndUsername(uuid, "renamed"); // on rename (same uuid but different name)
        Assertions.assertEquals(uuid, userMap.searchForUUID("renamed")); // obtain uuid by new name
        Assertions.assertNull(userMap.searchForUUID(name)); // cannot obtain uuid by old name

        Assertions.assertEquals(List.of(UserStorageTest.TEST_USER_1), userMap.getAllUsers());

        var expectedSnapshot = new Object2ObjectOpenHashMap<UUID, String>();
        expectedSnapshot.put(uuid, "renamed");
        Assertions.assertEquals(expectedSnapshot, userMap.getSnapshotIfDirty()); // should return entries to save
        Assertions.assertNull(userMap.getSnapshotIfDirty()); // should return null because user map is not modified
    }
}
