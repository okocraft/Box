package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.util.uuid.UUIDParser;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

// | uuid | username |
public class UserTable extends AbstractTable implements UserStorage {

    public UserTable(@NotNull Database database) {
        super(database, database.getSchemaSet().userTable());
    }

    @Override
    public void init() throws Exception {
        createTableAndIndex();
    }

    @Override
    public @NotNull BoxUser getUser(@NotNull UUID uuid) throws Exception {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT `username` FROM `%table%` WHERE `uuid`=? LIMIT 1")) {
            statement.setString(1, uuid.toString());

            try (var result = statement.executeQuery()) {
                var rawName = result.next() ? result.getString("username") : "";
                return BoxUserFactory.create(uuid, rawName.isEmpty() ? null : rawName);
            }
        }
    }

    @Override
    public void saveBoxUser(@NotNull UUID uuid, @Nullable String name) throws Exception {
        try (var connection = database.getConnection()) {
            var strUUID = uuid.toString();
            if (isExistingUser(connection, strUUID)) {
                updateUsername(connection, strUUID, Objects.requireNonNullElse(name, ""));
            } else {
                insertUser(connection, strUUID, Objects.requireNonNullElse(name, ""));
            }
        }
    }

    @Override
    public @Nullable BoxUser searchByName(@NotNull String name) throws Exception {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT * FROM `%table%` WHERE `username` LIKE ?")) {
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
    public @NotNull Collection<BoxUser> getAllUsers() throws Exception {
        var result = new ArrayList<BoxUser>();

        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT * FROM `%table%`")) {
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

    private boolean isExistingUser(@NotNull Connection connection, @NotNull String strUUID) throws SQLException {
        try (var statement = prepareStatement(connection, "SELECT `username` FROM `%table%` WHERE `uuid`=? LIMIT 1")) {
            statement.setString(1, strUUID);

            try (var result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void insertUser(@NotNull Connection connection, @NotNull String strUUID, @NotNull String name) throws SQLException {
        try (var statement = prepareStatement(connection, "INSERT INTO `%table%` (`uuid`, `username`) VALUES(?,?)")) {
            statement.setString(1, strUUID);
            statement.setString(2, name);

            statement.execute();
        }
    }

    private void updateUsername(@NotNull Connection connection, @NotNull String strUUID, @NotNull String name) throws SQLException {
        try (var statement = prepareStatement(connection, "UPDATE `%table%` SET `username`=? where `uuid`=?")) {
            statement.setString(1, strUUID);
            statement.setString(2, name);

            statement.execute();
        }
    }
}
