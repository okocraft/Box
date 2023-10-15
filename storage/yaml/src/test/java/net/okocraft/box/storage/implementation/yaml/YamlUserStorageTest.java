package net.okocraft.box.storage.implementation.yaml;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

class YamlUserStorageTest {

    private static final BoxUser TEST_USER_1 = BoxUserFactory.create(UUID.randomUUID(), "test_user_1");
    private static final BoxUser TEST_USER_2 = BoxUserFactory.create(UUID.randomUUID(), "test_user_2");

    @Test
    void testLoadingAndSaving(@TempDir Path dir) throws Exception {
        var storage = new YamlUserStorage(dir);
        storage.init();

        storage.saveBoxUser(TEST_USER_1.getUUID(), TEST_USER_1.getName().orElseThrow());
        storage.saveBoxUser(TEST_USER_2.getUUID(), TEST_USER_2.getName().orElseThrow());

        Assertions.assertEquals(TEST_USER_1, storage.loadBoxUser(TEST_USER_1.getUUID()));
        Assertions.assertEquals(TEST_USER_2, storage.loadBoxUser(TEST_USER_2.getUUID()));

        Assertions.assertEquals(TEST_USER_1, storage.searchByName("test_user_1"));
        Assertions.assertEquals(TEST_USER_2, storage.searchByName("TEST_USER_2"));

        var users = storage.loadAllBoxUsers();
        Assertions.assertEquals(2, users.size());
        Assertions.assertTrue(List.of(TEST_USER_1, TEST_USER_2).containsAll(users));

        var otherStorage = new YamlUserStorage(dir); // Test for loading users by UserStorage#init
        otherStorage.init();

        var otherUsers = otherStorage.loadAllBoxUsers();
        Assertions.assertEquals(2, users.size());
        Assertions.assertTrue(List.of(TEST_USER_1, TEST_USER_2).containsAll(otherUsers));
    }

    @Test
    void testGetUser(@TempDir Path dir) throws Exception {
        var storage = new YamlUserStorage(dir);
        storage.init();

        storage.saveBoxUser(TEST_USER_1.getUUID(), TEST_USER_1.getName().orElseThrow());

        Assertions.assertEquals(TEST_USER_1, storage.loadBoxUser(TEST_USER_1.getUUID()));
        Assertions.assertNotEquals(TEST_USER_2.getName(), storage.loadBoxUser(TEST_USER_2.getUUID()).getName());
        Assertions.assertEquals(BoxUserFactory.create(TEST_USER_2.getUUID()), storage.loadBoxUser(TEST_USER_2.getUUID()));
    }

    @Test
    void testUserMap() {
        var userMap = new YamlUserStorage.UserMap();

        var uuid = TEST_USER_1.getUUID();
        var name = TEST_USER_1.getName().orElseThrow();

        userMap.putUUIDAndUsername(uuid, name);

        Assertions.assertEquals(uuid, userMap.searchForUUID(name));
        Assertions.assertEquals(uuid, userMap.searchForUUID(name.toUpperCase(Locale.ENGLISH)));
        Assertions.assertNull(userMap.searchForUUID("test"));

        Assertions.assertEquals(name, userMap.searchForUsername(uuid));
        Assertions.assertNull(userMap.searchForUsername(UUID.randomUUID()));

        Assertions.assertEquals(List.of(TEST_USER_1), userMap.getAllUsers());

        var expectedSnapshot = new Object2ObjectOpenHashMap<UUID, String>();
        expectedSnapshot.put(uuid, name);
        Assertions.assertEquals(expectedSnapshot, userMap.getSnapshotIfDirty());
        Assertions.assertNull(userMap.getSnapshotIfDirty());
    }
}
