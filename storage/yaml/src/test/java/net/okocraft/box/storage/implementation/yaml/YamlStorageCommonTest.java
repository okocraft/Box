package net.okocraft.box.storage.implementation.yaml;

import net.okocraft.box.test.shared.storage.test.StockStorageTest;
import net.okocraft.box.test.shared.storage.test.UserStorageTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class YamlStorageCommonTest {

    @Nested
    class User {

        @Test
        void testLoadingAndSaving(@TempDir Path dir) throws Exception {
            UserStorageTest.testLoadingAndSaving(new YamlUserStorage(dir));
            UserStorageTest.testLoadingFromNewlyCreatedStorage(new YamlUserStorage(dir));
        }

        @Test
        void testRename(@TempDir Path dir) throws Exception {
            UserStorageTest.testRename(new YamlUserStorage(dir));
        }

    }

    @Nested
    class Stock {

        @Test
        void testLoadingAndSaving(@TempDir Path dir) throws Exception {
            StockStorageTest.testLoadingAndSaving(new YamlStockStorage(dir));
            StockStorageTest.testLoadingFromNewlyCreatedStorage(new YamlStockStorage(dir));
        }

        @Test
        void testCleaningZeroStock(@TempDir Path dir) throws Exception {
            StockStorageTest.testCleaningZeroStock(new YamlStockStorage(dir));
        }

    }

}
