package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.database.mysql.MySQLDatabase;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteDatabase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Objects;

// | key | value |
public class MetaTable extends AbstractTable {

    private static final String ITEM_DATA_VERSION_KEY = "item_data_version";
    private static final String DEFAULT_ITEM_VERSION_KEY = "default_item_version";
    private static final String CUSTOM_DATA_FORMAT_KEY = "custom_data_format";
    private static final String CURRENT_CUSTOM_DATA_FORMAT_VALUE = "configapi_v5_binary";

    private boolean hasItemDataVersion = false;
    private boolean hasDefaultItemVersion = false;

    public MetaTable(@NotNull Database database) {
        super(database, database.getSchemaSet().metaTable());
    }

    public void init() throws Exception {
        this.createTableAndIndex();
    }

    public @Nullable MCDataVersion getItemDataVersion() throws SQLException {
        var version = this.getVersion(ITEM_DATA_VERSION_KEY);

        if (version != null) {
            this.hasItemDataVersion = true;
            return MCDataVersion.of(version);
        } else {
            return null;
        }
    }

    public void saveItemDataVersion(int dataVersion) throws SQLException {
        this.saveVersion(ITEM_DATA_VERSION_KEY, dataVersion, this.hasItemDataVersion);
    }

    public int getDefaultItemProviderVersion() throws SQLException {
        var version = this.getVersion(DEFAULT_ITEM_VERSION_KEY);

        if (version != null) {
            this.hasDefaultItemVersion = true;
        }

        return Objects.requireNonNullElse(version, 0);
    }

    public void saveDefaultItemVersion(int version) throws SQLException {
        this.saveVersion(DEFAULT_ITEM_VERSION_KEY, version, this.hasDefaultItemVersion);
    }

    private @Nullable Integer getVersion(@NotNull String key) throws SQLException {
        try (var connection = this.database.getConnection();
             var statement = this.prepareStatement(connection, "SELECT `value` FROM `%table%` WHERE `key`=? LIMIT 1")) {
            statement.setString(1, key);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("value");
                }
            }
        }

        return null;
    }

    private void saveVersion(@NotNull String key, int version, boolean exists) throws SQLException {
        var sql = exists ?
                "UPDATE `%table%` SET value=? WHERE `key`=?" :
                "INSERT INTO `%table%` (`key`, `value`) VALUES(?,?)";

        try (var connection = this.database.getConnection();
             var statement = this.prepareStatement(connection, sql)) {
            if (exists) {
                statement.setInt(1, version);
                statement.setString(2, key);
            } else {
                statement.setString(1, key);
                statement.setInt(2, version);
            }

            statement.execute();
        }
    }

    public boolean isCurrentCustomDataFormat() throws SQLException {
        try (var connection = this.database.getConnection();
             var statement = this.prepareStatement(connection, "SELECT `value` FROM `%table%` WHERE `key`=? LIMIT 1")) {
            statement.setString(1, CUSTOM_DATA_FORMAT_KEY);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return CURRENT_CUSTOM_DATA_FORMAT_VALUE.equals(resultSet.getString("value"));
                }
            }
        }

        return false;
    }

    public void saveCurrentCustomDataFormat() throws SQLException {
        try (var connection = this.database.getConnection();
             var statement = this.prepareStatement(connection, this.upsertCurrentCustomDataFormatStatement())) {
            statement.setString(1, CUSTOM_DATA_FORMAT_KEY);
            statement.setString(2, CUSTOM_DATA_FORMAT_KEY);
            statement.execute();
        }
    }

    private @NotNull String upsertCurrentCustomDataFormatStatement() {
        if (this.database instanceof MySQLDatabase) {
            return "INSERT INTO `%table%` (`key`, `data`) VALUES (?, ?) AS new ON DUPLICATE KEY UPDATE `data` = new.data";
        } else if (this.database instanceof SQLiteDatabase) {
            return "INSERT INTO `%table%` (`key`, `data`) VALUES (?, ?) ON CONFLICT (`key`) DO UPDATE SET `data` = excluded.data";
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
