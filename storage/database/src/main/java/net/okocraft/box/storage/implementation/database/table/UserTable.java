package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.util.uuid.UUIDParser;
import net.okocraft.box.storage.implementation.database.database.Database;
import net.okocraft.box.storage.implementation.database.operator.UserTableOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

// | uuid | username |
public class UserTable implements UserStorage {

    private final Database database;
    private final UserTableOperator operator;

    public UserTable(@NotNull Database database) {
        this.database = database;
        this.operator = database.operators().userTable();
    }

    public void init(@NotNull Connection connection) throws Exception {
        this.operator.initTable(connection);
    }

    @Override
    public @NotNull BoxUser loadBoxUser(@NotNull UUID uuid) throws Exception {
        String username;

        try (var connection = this.database.getConnection()) {
            username = this.operator.selectUsernameByUUID(connection, uuid);
        }

        return BoxUserFactory.create(uuid, username);
    }

    @Override
    public void saveBoxUser(@NotNull UUID uuid, @Nullable String name) throws Exception {
        try (var connection = this.database.getConnection()) {
            this.operator.upsertUser(connection, uuid, name != null ? name : "");
        }
    }

    @Override
    public @Nullable BoxUser searchByName(@NotNull String name) throws Exception {
        if (name.isEmpty()) {
            return null;
        }

        String rawUuid;

        try (var connection = this.database.getConnection()) {
            rawUuid = this.operator.selectUUIDByUserName(connection, name);
        }

        if (rawUuid != null) {
            var uuid = UUIDParser.parseOrWarn(rawUuid);
            return uuid != null ? BoxUserFactory.create(uuid, name) : null;
        }

        return null;
    }

    @Override
    public @NotNull Collection<BoxUser> loadAllBoxUsers() throws Exception {
        var result = new ArrayList<BoxUser>();

        try (var connection = this.database.getConnection()) {
            this.operator.selectAllUsers(connection, (rawUuid, name) -> {
                var uuid = UUIDParser.parseOrWarn(rawUuid);
                if (uuid != null) {
                    result.add(BoxUserFactory.create(uuid, name));
                }
            });
        }

        return result;
    }

    @Override
    public void saveBoxUsers(@NotNull Collection<BoxUser> users) throws Exception {
        try (var connection = this.database.getConnection();
             var statement = this.operator.upsertUserStatement(connection)) {
            for (var user : users) {
                this.operator.addUpsertUserBatch(statement, user.getUUID(), user.getName().orElse(""));
            }
            statement.executeBatch();
        }
    }
}
