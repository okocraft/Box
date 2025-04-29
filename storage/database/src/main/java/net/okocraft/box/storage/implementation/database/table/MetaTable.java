package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.api.model.version.StorageVersion;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.operator.MetaTableOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

// | key | value |
public class MetaTable {

    private static final String ITEM_DATA_VERSION_KEY = "item_data_version";
    private static final String STORAGE_VERSION_KEY = "storage_version";
    private static final String CUSTOM_DATA_FORMAT_KEY = "custom_data_format";
    private static final String CURRENT_CUSTOM_DATA_FORMAT_VALUE = "configapi_v5_binary";

    private final Database database;
    private final MetaTableOperator operator;

    public MetaTable(@NotNull Database database) {
        this.database = database;
        this.operator = database.operators().metaTable();
    }

    public void init(@NotNull Connection connection) throws Exception {
        this.operator.initTable(connection);
    }

    public boolean exists(@NotNull Connection connection) throws Exception {
        return this.operator.existsTable(connection);
    }

    public @Nullable MCDataVersion getItemDataVersion() throws SQLException {
        var version = this.getVersion(ITEM_DATA_VERSION_KEY);

        if (version != null) {
            return MCDataVersion.of(version);
        } else {
            return null;
        }
    }

    public void saveItemDataVersion(@NotNull MCDataVersion version) throws SQLException {
        this.saveVersion(ITEM_DATA_VERSION_KEY, version.dataVersion());
    }

    public @NotNull StorageVersion getStorageVersion() throws SQLException {
        var version = this.getVersion(STORAGE_VERSION_KEY);

        if (version != null) {
            return new StorageVersion(version);
        } else {
            return StorageVersion.BEFORE_V6;
        }
    }

    public void saveStorageVersion(@NotNull StorageVersion version) throws SQLException {
        this.saveVersion(STORAGE_VERSION_KEY, version.value());
    }

    private @Nullable Integer getVersion(@NotNull String key) throws SQLException {
        try (var connection = this.database.getConnection()) {
            return this.operator.selectValueAsIntOrNull(connection, key);
        }
    }

    private void saveVersion(@NotNull String key, int version) throws SQLException {
        try (var connection = this.database.getConnection()) {
            this.operator.upsertValue(connection, key, String.valueOf(version));
        }
    }

    public boolean isCurrentCustomDataFormat() throws SQLException {
        try (var connection = this.database.getConnection()) {
            return CURRENT_CUSTOM_DATA_FORMAT_VALUE.equals(this.operator.selectValue(connection, CUSTOM_DATA_FORMAT_KEY));
        }
    }

    public void saveCurrentCustomDataFormat() throws SQLException {
        try (var connection = this.database.getConnection()) {
            this.operator.upsertValue(connection, CUSTOM_DATA_FORMAT_KEY, CURRENT_CUSTOM_DATA_FORMAT_VALUE);
        }
    }
}
