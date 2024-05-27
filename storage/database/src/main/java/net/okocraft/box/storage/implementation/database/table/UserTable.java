package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.util.uuid.UUIDParser;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.database.mysql.MySQLDatabase;
import net.okocraft.box.storage.implementation.database.database.sqlite.SQLiteDatabase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

// | uuid | username |
public class UserTable extends AbstractTable implements UserStorage {

    public UserTable(@NotNull Database database) {
        super(database, database.getSchemaSet().userTable());
    }

    @Override
    public void init() throws Exception {
        this.createTableAndIndex();
    }

    @Override
    public @NotNull BoxUser loadBoxUser(@NotNull UUID uuid) throws Exception {
        try (var connection = this.database.getConnection();
             var statement = this.prepareStatement(connection, "SELECT `username` FROM `%table%` WHERE `uuid`=? LIMIT 1")) {
            statement.setString(1, uuid.toString());

            try (var result = statement.executeQuery()) {
                var rawName = result.next() ? result.getString("username") : "";
                return BoxUserFactory.create(uuid, rawName.isEmpty() ? null : rawName);
            }
        }
    }

    @Override
    public void saveBoxUser(@NotNull UUID uuid, @Nullable String name) throws Exception {
        try (var connection = this.database.getConnection();
             var statement = this.prepareStatement(connection, this.insertOrUpdateUsernameStatement())) {
            statement.setString(1, uuid.toString());
            statement.setString(2, name != null ? name : "");
            statement.execute();
        }
    }

    @Override
    public @Nullable BoxUser searchByName(@NotNull String name) throws Exception {
        try (var connection = this.database.getConnection();
             var statement = this.prepareStatement(connection, "SELECT * FROM `%table%` WHERE `username` LIKE ?")) {
            statement.setString(1, name);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    var uuid = UUIDParser.parseOrWarn(result.getString("uuid"));
                    var username = result.getString("username");

                    if (uuid != null) {
                        return BoxUserFactory.create(uuid, username.isEmpty() ? null : username);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public @NotNull Collection<BoxUser> loadAllBoxUsers() throws Exception {
        var result = new ArrayList<BoxUser>();

        try (var connection = this.database.getConnection();
             var statement = this.prepareStatement(connection, "SELECT * FROM `%table%`")) {
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    var uuid = UUIDParser.parseOrWarn(resultSet.getString("uuid"));
                    var username = resultSet.getString("username");

                    if (uuid != null) {
                        result.add(BoxUserFactory.create(uuid, username.isEmpty() ? null : username));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void saveBoxUsers(@NotNull Collection<BoxUser> users) throws Exception {
        try (var connection = this.database.getConnection();
             var statement = this.prepareStatement(connection, this.insertOrUpdateUsernameStatement())) {
            for (var user : users) {
                statement.setString(1, user.getUUID().toString());
                statement.setString(2, user.getName().orElse(""));
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private @NotNull String insertOrUpdateUsernameStatement() {
        if (this.database instanceof MySQLDatabase) {
            return "INSERT INTO `%table%` (`uuid`, `username`) VALUES (?, ?) AS new ON DUPLICATE KEY UPDATE `username` = new.username";
        } else if (this.database instanceof SQLiteDatabase) {
            return "INSERT INTO `%table%` (`uuid`, `username`) VALUES (?, ?) ON CONFLICT (`uuid`) DO UPDATE SET `username` = excluded.username";
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
