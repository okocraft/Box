package net.okocraft.box.core.model.manager.user;

import net.okocraft.box.test.shared.storage.memory.user.MemoryUserStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static net.okocraft.box.test.shared.model.user.TestUser.USER;

class UserManagerTest {

    @Test
    void test() {
        var storage = new MemoryUserStorage();
        var manager = new BoxUserManager(storage);

        var uuid = USER.getUUID();
        var username = USER.getName().orElseThrow();
        manager.saveUsername(USER);

        Assertions.assertEquals(USER, storage.loadBoxUser(uuid));
        Assertions.assertEquals(username, storage.loadBoxUser(uuid).getName().orElseThrow());

        var loadedUser = manager.loadBoxUser(uuid);

        Assertions.assertEquals(USER, loadedUser);
        Assertions.assertEquals(username, loadedUser.getName().orElseThrow());

        var searchResult = manager.searchByName(username);

        Assertions.assertNotNull(searchResult);
        Assertions.assertEquals(USER, searchResult);

        Assertions.assertNull(manager.searchByName("test_user_2"));
    }

}
