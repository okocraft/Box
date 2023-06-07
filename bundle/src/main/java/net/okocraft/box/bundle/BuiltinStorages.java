package net.okocraft.box.bundle;

import com.github.siroshun09.configapi.api.Configuration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import net.okocraft.box.storage.implementation.database.DatabaseStorage;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.database.mysql.MySQLConfig;
import net.okocraft.box.storage.implementation.database.database.mysql.MySQLDatabase;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteDatabase;
import net.okocraft.box.storage.implementation.yaml.YamlStorage;
import org.jetbrains.annotations.NotNull;

public final class BuiltinStorages {

    public static void addToRegistry(@NotNull StorageRegistry registry) {
        registry.register(YamlStorage.STORAGE_NAME, BuiltinStorages::createYamlStorage);
        registry.setDefaultStorageName(YamlStorage.STORAGE_NAME);

        registry.register(Database.Type.SQLITE.getName(), BuiltinStorages::createSQLiteStorage);
        registry.register(Database.Type.MYSQL.getName(), BuiltinStorages::createMySQLStorage);
    }

    public static @NotNull Storage createYamlStorage(@NotNull Configuration config) {
        var dirName = config.getString("yaml.directory-name", "data");
        return new YamlStorage(BoxProvider.get().getPluginDirectory().resolve(dirName));
    }

    private static @NotNull Storage createSQLiteStorage(@NotNull Configuration config) {
        return new DatabaseStorage(
                new SQLiteDatabase(
                        BoxProvider.get().getPluginDirectory().resolve(config.getString("sqlite.filename", "box-sqlite.db")),
                        config.getString("sqlite.table-prefix", config.getString("database.table_prefix", "box_")) // Refer to the old configuration key.
                )
        );
    }

    private static @NotNull Storage createMySQLStorage(@NotNull Configuration config) {
        return new DatabaseStorage(
                new MySQLDatabase(new MySQLConfig(config.getOrCreateSection("mysql")))
        );
    }

    private BuiltinStorages() {
        throw new UnsupportedOperationException();
    }
}
