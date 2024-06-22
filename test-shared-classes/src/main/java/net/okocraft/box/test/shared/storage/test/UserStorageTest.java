package net.okocraft.box.test.shared.storage.test;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public abstract class UserStorageTest<S> extends AbstractStorageTest<S> {

    public static final BoxUser TEST_USER_1 = BoxUserFactory.create(UUID.randomUUID(), "test_user_1");
    public static final BoxUser TEST_USER_2 = BoxUserFactory.create(UUID.randomUUID(), "test_user_2");

    private static void save(@NotNull UserStorage storage, @NotNull BoxUser user) throws Exception {
        storage.saveBoxUser(user.getUUID(), user.getName().orElseThrow());
    }

    private static void checkLoad(@NotNull UserStorage storage, @NotNull BoxUser expected) throws Exception {
        Assertions.assertEquals(expected, storage.loadBoxUser(expected.getUUID())); // can load user by uuid
        Assertions.assertEquals(expected, storage.searchByName(expected.getName().orElseThrow())); // can load user by name
        Assertions.assertEquals(expected, storage.searchByName(expected.getName().orElseThrow().toUpperCase(Locale.ENGLISH))); // can load user by name (case-insensitive)
    }

    @Test
    void testLoadingAndSaving() throws Exception {
        var storage = this.newStorage();
        var userStorage = this.newUserStorage(storage);

        try {
            save(userStorage, TEST_USER_1);
            checkLoad(userStorage, TEST_USER_1);

            save(userStorage, TEST_USER_2);
            checkLoad(userStorage, TEST_USER_2);

            var users = userStorage.loadAllBoxUsers();
            Assertions.assertEquals(2, users.size());
            Assertions.assertTrue(List.of(TEST_USER_1, TEST_USER_2).containsAll(users));
        } finally {
            this.closeStorage(storage);
        }
    }

    @Test
    void testRename() throws Exception {
        var storage = this.newStorage();
        var userStorage = this.newUserStorage(storage);

        try {
            save(userStorage, TEST_USER_1);
            checkLoad(userStorage, TEST_USER_1);

            var renamed = BoxUserFactory.create(TEST_USER_1.getUUID(), "renamed_test_user_1");
            save(userStorage, renamed);
            checkLoad(userStorage, renamed);
            Assertions.assertNull(userStorage.searchByName(TEST_USER_1.getName().orElseThrow())); // cannot obtain user by old name
        } finally {
            this.closeStorage(storage);
        }
    }

    protected abstract @NotNull UserStorage newUserStorage(@NotNull S storage) throws Exception;

}
