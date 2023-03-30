package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.api.util.MCDataVersion;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Objects;

// | key | value |
public class MetaTable extends AbstractTable {

    private static final String ITEM_DATA_VERSION_KEY = "item_data_version";
    private static final String DEFAULT_ITEM_VERSION_KEY = "default_item_version";

    private boolean hasItemDataVersion = false;
    private boolean hasDefaultItemVersion = false;

    public MetaTable(@NotNull Database database) {
        super(database, database.getSchemaSet().metaTable());
    }

    public void init() throws Exception {
        createTableAndIndex();
    }

    public @Nullable MCDataVersion getItemDataVersion() throws SQLException {
        var version = getVersion(ITEM_DATA_VERSION_KEY);

        if (version != null) {
            hasItemDataVersion = true;
            return MCDataVersion.of(version);
        } else {
            return null;
        }
    }

    public void saveItemDataVersion(int dataVersion) throws SQLException {
        saveVersion(ITEM_DATA_VERSION_KEY, dataVersion, hasItemDataVersion);
    }

    public int getDefaultItemVersion() throws SQLException {
        var version = getVersion(DEFAULT_ITEM_VERSION_KEY);

        if (version != null) {
            hasDefaultItemVersion = true;
        }

        return Objects.requireNonNullElse(version, 0);
    }

    public void saveDefaultItemVersion(int version) throws SQLException {
        saveVersion(DEFAULT_ITEM_VERSION_KEY, version, hasDefaultItemVersion);
    }

    private @Nullable Integer getVersion(@NotNull String key) throws SQLException {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT `value` FROM `%table%` WHERE `key`=? LIMIT 1")) {
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

        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, sql)) {
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
}
