package net.okocraft.box.core.model.manager.user;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.test.shared.storage.memory.user.MemoryUserStorage;
import net.okocraft.box.test.shared.util.LogCollector;
import net.okocraft.box.test.shared.util.UUIDFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoxUserManagerTest {

    private static final String TEST_USER_NAME = "test";
    private static final UUID TEST_USER_UUID = UUIDFactory.byName(TEST_USER_NAME);
    private static final BoxUser TEST_USER = BoxUserFactory.create(TEST_USER_UUID, TEST_USER_NAME);

    @Test
    void testCreate() {
        BoxUserManager manager = createUserManager();

        // Create a user without name.
        checkUserWithoutName(TEST_USER_UUID, manager.createBoxUser(TEST_USER_UUID));

        // Create a user with the UUID and name.
        checkUser(manager.createBoxUser(TEST_USER_UUID, TEST_USER_NAME));
    }

    @Test
    void testLoadAndSave() {
        BoxUserManager manager = createUserManager();

        // The manager does not have any users, so the method returns the user with the given UUID.
        checkUserWithoutName(TEST_USER_UUID, manager.loadBoxUser(TEST_USER_UUID));

        // Save a user to the manager.
        manager.saveUsername(TEST_USER);

        // Check if the saved user is loadable.
        checkUser(manager.loadBoxUser(TEST_USER_UUID));

        // Check if the unknown user is still not loadable.
        UUID unknownUserUuid = UUIDFactory.byName("unknown");
        checkUserWithoutName(unknownUserUuid, manager.loadBoxUser(unknownUserUuid));
    }

    @Test
    void testSearch() {
        BoxUserManager manager = createUserManager();

        // The manager does not have any users, so the method returns null.
        assertNull(manager.searchByName(TEST_USER_NAME));

        // Save a user to the manager.
        manager.saveUsername(TEST_USER);

        // Check if the saved user is searchable.
        checkUser(manager.searchByName(TEST_USER_NAME));

        // Check if the unknown user is still not searchable.
        assertNull(manager.searchByName("unknown"));
    }

    @Test
    void testExceptionHandling() {
        MemoryUserStorage storage = new MemoryUserStorage();
        storage.setThrowExceptionMode(true); // Enable throwing an exception on every method call.

        LogCollector logCollector = new LogCollector();

        try {
            logCollector.injectToBoxLogger();

            BoxUserManager manager = new BoxUserManager(storage);

            // BoxUserManager#loadBoxUser
            // Return value is the user with the given UUID
            checkUserWithoutName(TEST_USER_UUID, manager.loadBoxUser(TEST_USER_UUID));
            logCollector.checkLog(Level.ERROR, "Could not load the user ({})", TEST_USER_UUID, storage.getThrownException());

            // BoxUserManager#searchByName
            // Return value is null
            assertNull(manager.searchByName(TEST_USER_NAME));
            logCollector.checkLog(Level.ERROR, "Could not search for the user by name ({})", TEST_USER_NAME, storage.getThrownException());

            // BoxUserManager#saveUsername
            manager.saveUsername(TEST_USER);
            logCollector.checkLog(Level.ERROR, "Could not save the user (uuid: {} name: {})", TEST_USER_UUID, TEST_USER_NAME, storage.getThrownException());
        } finally {
            logCollector.ejectFromBoxLogger();
        }
    }

    private static void checkUser(@Nullable BoxUser actualUser) {
        assertEquals(TEST_USER, actualUser);
    }

    private static void checkUserWithoutName(@NotNull UUID expectedUuid, @Nullable BoxUser actualUser) {
        assertNotNull(actualUser);
        assertEquals(expectedUuid, actualUser.getUUID());
        assertTrue(actualUser.getName().isEmpty());
    }

    private static @NotNull BoxUserManager createUserManager() {
        return new BoxUserManager(new MemoryUserStorage());
    }
}
