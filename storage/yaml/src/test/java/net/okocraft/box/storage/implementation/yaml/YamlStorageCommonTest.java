package net.okocraft.box.storage.implementation.yaml;

import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.CustomItemStorage;
import net.okocraft.box.storage.api.model.item.DefaultItemStorage;
import net.okocraft.box.storage.api.model.item.RemappedItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.test.shared.storage.test.CustomDataStorageTest;
import net.okocraft.box.test.shared.storage.test.CustomItemStorageTest;
import net.okocraft.box.test.shared.storage.test.DefaultItemStorageTest;
import net.okocraft.box.test.shared.storage.test.RemappedItemStorageTest;
import net.okocraft.box.test.shared.storage.test.StockStorageTest;
import net.okocraft.box.test.shared.storage.test.UserStorageTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class YamlStorageCommonTest {

    @TempDir
    private Path dir;

    @Nested
    class User extends UserStorageTest<Path> {
        @Override
        protected @NotNull Path newStorage() {
            return YamlStorageCommonTest.this.dir;
        }

        @Override
        protected @NotNull UserStorage newUserStorage(@NotNull Path path) throws Exception {
            var storage = new YamlUserStorage(path);
            storage.init();
            return storage;
        }
    }

    @Nested
    class DefaultItem extends DefaultItemStorageTest<Path> {
        @Override
        protected @NotNull Path newStorage() {
            return YamlStorageCommonTest.this.dir;
        }

        @Override
        protected @NotNull DefaultItemStorage newDefaultItemStorage(@NotNull Path path) {
            return new YamlDefaultItemStorage(path, new YamlMetaStorage(path));
        }
    }

    @Nested
    class CustomItem extends CustomItemStorageTest<Path> {
        @Override
        protected @NotNull Path newStorage() {
            return YamlStorageCommonTest.this.dir;
        }

        @Override
        protected @NotNull CustomItemStorage newCustomItemStorage(@NotNull Path path) {
            return new YamlCustomItemStorage(path, new YamlMetaStorage(path));
        }
    }

    @Nested
    class RemappedItem extends RemappedItemStorageTest<Path> {
        @Override
        protected @NotNull Path newStorage() {
            return YamlStorageCommonTest.this.dir;
        }

        @Override
        protected @NotNull RemappedItemStorage newRemappedItemStorage(@NotNull Path path) throws Exception {
            return new YamlRemappedItemStorage(path);
        }
    }

    @Nested
    class Stock extends StockStorageTest<Path> {
        @Override
        protected @NotNull Path newStorage() {
            return YamlStorageCommonTest.this.dir;
        }

        @Override
        protected @NotNull StockStorage newStockStorage(@NotNull Path path) throws Exception {
            var storage = new YamlStockStorage(path);
            storage.init();
            return storage;
        }
    }

    @Nested
    class CustomData extends CustomDataStorageTest<Path> {
        @Override
        protected @NotNull Path newStorage() {
            return YamlStorageCommonTest.this.dir;
        }

        @Override
        protected @NotNull CustomDataStorage newCustomDataStorage(@NotNull Path path) throws Exception {
            var storage = new YamlCustomDataStorage(path);
            storage.init();
            return storage;
        }
    }
}
