package net.okocraft.box.plugin.database.table;

import net.okocraft.box.plugin.database.connector.Database;
import net.okocraft.box.plugin.model.User;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerTable {

    private final static String UNKNOWN_NAME = "unknown";
    private final static String USER_SELECT_BY_UUID = "select id, name from %table% where uuid=? limit 1";
    private final static String USER_SELECT_BY_NAME = "select id, uuid from %table% where name=? limit=1";
    private final static String USER_SELECT_BY_ID = "select uuid, name from %table% where id=? limit=1";
    private final static String USER_SELECT_ID_BY_UUID = "select id from %table% where uuid=? limit=1";
    private final static String USER_REPLACE_NAME_TO = "select name, replace(name, ?, ?) from %table%";
    private final static String USER_RENAME = "update %table% set name=? where uuid=? limit 1";
    private final static String INSERT_USER = "insert into %table% (uuid, name) values(?,?)";

    private final Database database;
    private final String tableName;

    public PlayerTable(@NotNull Database database, @NotNull String prefix) {
        this.database = database;
        tableName = prefix + "players";

        try {
            createTable();
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @NotNull
    public User loadUser(@NotNull UUID uuid) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(USER_SELECT_BY_UUID))) {

            st.setString(1, uuid.toString());

            ResultSet result = st.executeQuery();

            int id;
            String name;

            if (result.next()) {
                id = result.getInt("id");
                name = Objects.requireNonNullElse(result.getString("name"), UNKNOWN_NAME);
                result.close();
                return new User(id, uuid, name);
            }
        }

        return createUser(uuid, UNKNOWN_NAME);
    }

    public User updateUser(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        boolean newPlayer = false;
        boolean renamed = false;

        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(USER_SELECT_BY_UUID))) {

            st.setString(1, uuid.toString());

            ResultSet result = st.executeQuery();

            if (result.next()) {
                renamed = Optional.ofNullable(result.getString("name")).map(s -> !s.equals(name)).orElse(true);
            } else {
                newPlayer = true;
            }

            result.close();
        }

        if (newPlayer) {
            return createUser(uuid, name);
        }

        if (renamed) {
            rename(uuid, name);
        }

        return loadUser(uuid);
    }

    @NotNull
    public Optional<User> searchUser(@NotNull String username) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(USER_SELECT_BY_NAME))) {

            st.setString(1, username);

            ResultSet result = st.executeQuery();

            if (result.next()) {
                Optional<UUID> uuid = toUUID(Objects.requireNonNullElse(result.getString("uuid"), ""));

                if (uuid.isPresent()) {
                    int id = result.getInt("id");
                    result.close();
                    return Optional.of(new User(id, uuid.get(), username));
                }
            }

            result.close();
            return Optional.empty();
        }
    }

    @NotNull
    public Optional<User> searchUserByID(int internalID) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(USER_SELECT_BY_ID))) {

            st.setInt(1, internalID);

            ResultSet result = st.executeQuery();

            if (result.next()) {
                Optional<UUID> uuid = toUUID(Objects.requireNonNullElse(result.getString("uuid"), ""));

                if (uuid.isPresent()) {
                    String name = Objects.requireNonNullElse(result.getString("name"), UNKNOWN_NAME);
                    result.close();
                    return Optional.of(new User(internalID, uuid.get(), name));
                }
            }

            result.close();
            return Optional.empty();
        }
    }

    @NotNull
    private User createUser(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(INSERT_USER))) {

            st.setString(1, uuid.toString());
            st.setString(2, name);

            st.execute();
        }

        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(USER_SELECT_ID_BY_UUID))) {

            st.setString(1, uuid.toString());

            ResultSet result = st.executeQuery();
            int id;

            if (result.next()) {
                id = result.getInt("id");
            } else {
                id = -1;
            }

            result.close();
            return new User(id, uuid, name);
        }
    }

    private void rename(@NotNull UUID uuid, @NotNull String newName) throws SQLException {
        checkSameName(newName);

        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(USER_RENAME))) {

            st.setString(1, newName);
            st.setString(2, uuid.toString());

            st.execute();
        }
    }

    private void checkSameName(@NotNull String name) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(USER_REPLACE_NAME_TO))) {

            st.setString(1, name);
            st.setString(2, UNKNOWN_NAME);

            st.execute();
        }
    }

    private void createTable() throws SQLException {
        try (Connection c = database.getConnection(); Statement statement = c.createStatement()) {
            String autoIncrement = database.getType() == Database.Type.SQLITE ? "AUTOINCREMENT" : "AUTO_INCREMENT";

            statement.addBatch(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "id INTEGER PRIMARY KEY " + autoIncrement + ", " +
                            "uuid CHAR(36) UNIQUE NOT NULL, " +
                            "name VARCHAR(16) UNIQUE NOT NULL)"
            );

            statement.executeBatch();
        }
    }

    @NotNull
    private String replaceTableName(@NotNull String sql) {
        return sql.replace("%table%", tableName);
    }

    @NotNull
    private Optional<UUID> toUUID(@NotNull String strUUID) {
        try {
            return Optional.of(UUID.fromString(strUUID));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
