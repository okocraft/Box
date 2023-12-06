package net.okocraft.box.core.config;

import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.model.data.CustomDataStorage;
import net.okocraft.box.storage.api.model.item.ItemStorage;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.registry.StorageContext;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import net.okocraft.box.storage.memory.MemoryStorage;
import net.okocraft.box.storage.memory.MemoryStorageSetting;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

class ConfigTest {

    private static final String EXPECTED_DEFAULT_CONFIG =
            """
                    #
                    # The core settings of Box.
                    #
                    core:
                      # The list of worlds where Box cannot be used.
                      disabled-worlds: []
                      # Whether to enable debug mode or not.
                      debug: false
                    #
                    # The settings of storage that will be used for loading/saving data.
                    #
                    storage:
                      type: memory # The type of storage to be used.
                      memory:
                        # Whether to enable partial saving.
                        partial-saving: true
                        example-value: 10 # An example setting to test generating default config.
                    """;

    private static final String CUSTOM_CORE_SETTING =
            """
                    core:
                      disabled-worlds:
                        - example_world_1
                        - example_world_2
                      debug: true
                    """;

    private static final String CUSTOM_EDITED_CORE_SETTING =
            """
                    core:
                      disabled-worlds:
                        - example_world_1
                      debug: false
                    """;

    private static final String WITH_INVALID_STORAGE_TYPE =
            """
                    storage:
                      type: invalid
                    """;

    private static final String ONLY_MEMORY_STORAGE_TYPE =
            """
                    storage:
                      type: memory
                    """;

    @Test
    void testInitialLoad(@TempDir Path dir) throws Exception {
        Files.createDirectories(dir);
        var registry = storageRegistry();

        registry.register("memory", MemoryStorageSetting.class, MemoryStorage::new);

        registry.setDefaultStorageName("memory");

        var config = new Config(dir);
        var storage = Assertions.assertInstanceOf(MemoryStorage.class, config.loadAndCreateStorage(registry));

        Assertions.assertNotNull(config.coreSetting());
        Assertions.assertEquals(config.coreSetting(), new CoreSetting(Collections.emptySet(), false));
        Assertions.assertEquals(storage.getSetting(), new MemoryStorageSetting(true, 10));

        Assertions.assertEquals(EXPECTED_DEFAULT_CONFIG, Files.readString(config.filepath()));
    }

    @Test
    void testCoreSetting(@TempDir Path dir) throws Exception {
        var config = new Config(dir);
        Files.writeString(config.filepath(), CUSTOM_CORE_SETTING);
        config.loadAndCreateStorage(storageRegistry());

        Assertions.assertEquals(new CoreSetting(Set.of("example_world_1", "example_world_2"), true), config.coreSetting());

        Files.writeString(config.filepath(), CUSTOM_EDITED_CORE_SETTING);
        config.reload();
        Assertions.assertEquals(new CoreSetting(Set.of("example_world_1"), false), config.coreSetting());
    }

    @Test
    void testCreateStorage(@TempDir Path dir) throws Exception {
        var config = new Config(dir);
        Files.writeString(config.filepath(), WITH_INVALID_STORAGE_TYPE);
        var registry = storageRegistry();

        registry.register("memory", MemoryStorageSetting.class, MemoryStorage::new);

        Assertions.assertInstanceOf(DummyStorage.class, config.loadAndCreateStorage(registry));

        Files.writeString(config.filepath(), ONLY_MEMORY_STORAGE_TYPE);

        Assertions.assertInstanceOf(MemoryStorage.class, config.loadAndCreateStorage(registry));
    }

    private static @NotNull StorageRegistry storageRegistry() {
        var registry = new StorageRegistry();

        registry.register("dummy", DummyStorageSetting.class, DummyStorage::new);
        registry.setDefaultStorageName("dummy");

        return registry;
    }

    private record DummyStorageSetting() {
    }

    private static class DummyStorage implements Storage {

        private DummyStorage(@NotNull StorageContext<DummyStorageSetting> context) {
        }

        @Override
        public @NotNull String getName() {
            return "dummy";
        }

        @Override
        public @NotNull List<Property> getInfo() {
            return Collections.emptyList();
        }

        @Override
        public void init() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull ItemStorage getItemStorage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull UserStorage getUserStorage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull StockStorage getStockStorage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull CustomDataStorage getCustomDataStorage() {
            throw new UnsupportedOperationException();
        }
    }
}
