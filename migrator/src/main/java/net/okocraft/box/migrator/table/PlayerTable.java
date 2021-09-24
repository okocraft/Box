package net.okocraft.box.migrator.table;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.migrator.database.Database;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/*
 * source:
 * https://github.com/okocraft/Box/blob/master/src/main/java/net/okocraft/box/database/PlayerTable.java
 */
@SuppressWarnings("ClassCanBeRecord")
public class PlayerTable {

    private final Database database;

    public PlayerTable(@NotNull Database database) {
        this.database = database;
    }

    public @NotNull Map<BoxUser, Integer> load() {
        var userIdMap = new HashMap<BoxUser, Integer>();

        database.execute("SELECT id, uuid, name FROM box_players", rs -> {
            while (rs.next()) {
                var id = rs.getInt("id");
                var uuid = UUID.fromString(rs.getString("uuid"));
                var name = rs.getString("name");

                userIdMap.put(new MigratedBoxUser(uuid, name), id);
            }
        });

        return userIdMap;
    }

    private record MigratedBoxUser(@NotNull UUID uuid, @NotNull String name) implements BoxUser {

        @Override
        public @NotNull UUID getUUID() {
            return uuid;
        }

        @Override
        public @NotNull Optional<String> getName() {
            return Optional.of(name);
        }
    }
}
