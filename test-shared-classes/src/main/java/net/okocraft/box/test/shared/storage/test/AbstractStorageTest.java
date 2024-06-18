package net.okocraft.box.test.shared.storage.test;

import org.jetbrains.annotations.NotNull;

abstract class AbstractStorageTest<S> {

    protected abstract @NotNull S newStorage() throws Exception;

    protected void closeStorage(@NotNull S storage) throws Exception {
        if (storage instanceof AutoCloseable autoCloseable) {
            autoCloseable.close();
        }
    }
}
