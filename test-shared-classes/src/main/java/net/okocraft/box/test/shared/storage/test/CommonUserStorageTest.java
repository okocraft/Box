package net.okocraft.box.test.shared.storage.test;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public abstract class CommonUserStorageTest {

    protected static final BoxUser TEST_USER_1 = BoxUserFactory.create(UUID.randomUUID(), "test_user_1");
    protected static final BoxUser TEST_USER_2 = BoxUserFactory.create(UUID.randomUUID(), "test_user_2");

    protected void testLoadingAndSaving(@NotNull UserStorage storage) throws Exception {
        storage.init();

        save(storage, TEST_USER_1);
        checkLoad(storage, TEST_USER_1);

        save(storage, TEST_USER_2);
        checkLoad(storage, TEST_USER_2);

        var users = storage.loadAllBoxUsers();
        Assertions.assertEquals(2, users.size());
        Assertions.assertTrue(List.of(TEST_USER_1, TEST_USER_2).containsAll(users));
    }

    protected void testLoadingFromNewlyCreatedStorage(@NotNull UserStorage storage) throws Exception {
        storage.init();

        checkLoad(storage, TEST_USER_1);
        checkLoad(storage, TEST_USER_2);

        var users = storage.loadAllBoxUsers();
        Assertions.assertEquals(2, users.size());
        Assertions.assertTrue(List.of(TEST_USER_1, TEST_USER_2).containsAll(users));
    }

    protected void testRename(@NotNull UserStorage storage) throws Exception {
        storage.init();

        save(storage, TEST_USER_1);
        checkLoad(storage, TEST_USER_1);

        var renamed = BoxUserFactory.create(TEST_USER_1.getUUID(), "renamed_test_user_1");
        save(storage, renamed);
        checkLoad(storage, renamed);
        Assertions.assertNull(storage.searchByName(TEST_USER_1.getName().orElseThrow())); // cannot obtain user by old name
    }

    private static void save(@NotNull UserStorage storage, @NotNull BoxUser user) throws Exception {
        storage.saveBoxUser(user.getUUID(), user.getName().orElseThrow());
    }

    private static void checkLoad(@NotNull UserStorage storage, @NotNull BoxUser expected) throws Exception {
        Assertions.assertEquals(expected, storage.loadBoxUser(expected.getUUID())); // can load user by uuid
        Assertions.assertEquals(expected, storage.searchByName(expected.getName().orElseThrow())); // can load user by name
        Assertions.assertEquals(expected, storage.searchByName(expected.getName().orElseThrow().toUpperCase(Locale.ENGLISH))); // can load user by name (case-insensitive)
    }
}
