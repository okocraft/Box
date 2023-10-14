package net.okocraft.box.core.model.manager.user;

import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.memory.user.MemoryUserStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class UserManagerTest {

    @Test
    void test() {
        var storage = new MemoryUserStorage();
        var manager = new BoxUserManager(storage);

        var user = BoxUserFactory.create(UUID.randomUUID(), "test_user");
        manager.saveUsername(user);

        Assertions.assertEquals(user, storage.getUser(user.getUUID()));
        Assertions.assertEquals("test_user", storage.getUser(user.getUUID()).getName().orElseThrow());

        var loadedUser = manager.loadBoxUser(user.getUUID());

        Assertions.assertEquals(user, loadedUser);
        Assertions.assertEquals("test_user", loadedUser.getName().orElseThrow());

        var searchResult = manager.searchByName("test_user");

        Assertions.assertNotNull(searchResult);
        Assertions.assertEquals(user, searchResult);

        Assertions.assertNull(manager.searchByName("test_user_2"));
    }

}
