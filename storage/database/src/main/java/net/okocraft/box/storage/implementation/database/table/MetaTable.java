package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

// | key | value |
public class MetaTable extends AbstractTable {

    private static final String ITEM_DATA_VERSION_KEY = "item_data_version";

    private boolean hasItemDataVersion = false;

    public MetaTable(@NotNull Database database) {
        super(database, database.getSchemaSet().metaTable());
    }

    public void init() throws Exception {
        createTableAndIndex();
    }

    public int getItemDataVersion() throws SQLException {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT value FROM `%table%` WHERE key=? LIMIT 1")) {
            statement.setString(1, ITEM_DATA_VERSION_KEY);

            try (var resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    hasItemDataVersion = true;
                    return resultSet.getInt("value");
                }
            }
        }

        return 0;
    }

    public void saveItemDataVersion(int dataVersion) throws SQLException {
        var sql = hasItemDataVersion ?
                "UPDATE `%table%` SET value=? WHERE key=?" :
                "INSERT INTO `%table%` (key, value) VALUES(?,?)";

        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, sql)) {
            if (hasItemDataVersion) {
                statement.setInt(1, dataVersion);
                statement.setString(2, ITEM_DATA_VERSION_KEY);
            } else {
                statement.setString(1, ITEM_DATA_VERSION_KEY);
                statement.setInt(2, dataVersion);
            }

            statement.execute();
        }
    }
}
