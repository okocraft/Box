package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.util.uuid.UUIDParser;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
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
             var statement = prepareStatement(connection, "SELECT username FROM `%table%` WHERE uuid=? LIMIT 1")) {
            statement.setString(1, uuid.toString());

            try (var result = statement.executeQuery()) {
                var rawName = result.next() ? result.getString("username") : "";
                return BoxUserFactory.create(uuid, rawName.isEmpty() ? null : rawName);
            }
        }
    }

    @Override
    public void saveBoxUser(@NotNull BoxUser user) throws Exception {
        try (var connection = database.getConnection()) {
            if (isExistingUser(connection, user)) {
                updateUsername(connection, user);
            } else {
                insertUser(connection, user);
            }
        }
    }

    @Override
    public void saveBoxUserIfNotExists(@NotNull BoxUser user) throws Exception {
        try (var connection = database.getConnection()) {
            if (!isExistingUser(connection, user)) {
                insertUser(connection, user);
            }
        }
    }

    @Override
    public @NotNull Optional<BoxUser> search(@NotNull String name) throws Exception {
        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT * FROM `%table%` WHERE username LIKE ?")) {
            statement.setString(1, name);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    var uuid = UUIDParser.parseOrWarn(result.getString("uuid"));
                    var username = result.getString("username");
                    return uuid != null ?
                            Optional.of(BoxUserFactory.create(uuid, username.isEmpty() ? null : username)) :
                            Optional.empty();
                }
            }
        }

        return Optional.empty();
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

    private boolean isExistingUser(@NotNull Connection connection, @NotNull BoxUser user) throws SQLException {
        try (var statement = prepareStatement(connection, "SELECT username FROM `%table%` WHERE uuid=? LIMIT 1")) {
            statement.setString(1, user.getUUID().toString());

            try (var result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private void insertUser(@NotNull Connection connection, @NotNull BoxUser user) throws SQLException {
        try (var statement = prepareStatement(connection, "INSERT INTO `%table%` (uuid, username) VALUES(?,?)")) {
            statement.setString(1, user.getUUID().toString());
            statement.setString(2, user.getName().orElse(""));

            statement.execute();
        }
    }

    private void updateUsername(@NotNull Connection connection, @NotNull BoxUser user) throws SQLException {
        try (var statement = prepareStatement(connection, "UPDATE `%table%` SET username=? where uuid=?")) {
            statement.setString(1, user.getName().orElse(""));
            statement.setString(2, user.getUUID().toString());

            statement.execute();
        }
    }
}
