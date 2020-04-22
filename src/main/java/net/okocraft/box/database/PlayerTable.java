package net.okocraft.box.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * このクラスで扱うアイテムのIDは、プレイヤーのデータを保存しているテーブルのcolumnとなる。このクラスはメタを持たない単純なアイテムは常に全てのテーブルにあることを保証する。
 */
final class PlayerTable {

    static final String TABLE = "box_players";

    private final Database database;

    private final BiMap<Player, Integer> idCache = HashBiMap.create();

    PlayerTable(@NotNull Database database) {
        this.database = database;
        database.execute("CREATE TABLE IF NOT EXISTS " + TABLE + " (id INTEGER PRIMARY KEY " + (database.isSQLite() ? "AUTOINCREMENT" : "AUTO_INCREMENT") + ", uuid CHAR(36) UNIQUE NOT NULL, name VARCHAR(16) UNIQUE NOT NULL)");
    }

    private int insert(OfflinePlayer player) {
        if (player == null || player.getName() == null) {
            return -1;
        }

        database.execute("UPDATE " + TABLE + " SET name = '' WHERE name = '" + player.getName() + "'");
        String sql = "INSERT INTO " + TABLE + " (uuid, name) VALUES ('" + player.getUniqueId() + "', '" + player.getName() + "')";
        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement(sql,
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.executeUpdate();
            return preparedStatement.getGeneratedKeys().getInt(1);
        } catch (SQLException e) {
            System.err.println("Error occurred on executing SQL: " + sql);
            e.printStackTrace();
            return -1;
        }
    }

    void loadCache(Player player) {
        idCache.put(player, getId(player.getUniqueId()));
    }

    void removeCache(Player player) {
        idCache.remove(player);
    }

    /**
     * すでに登録済みのアイテムのIDを返す。登録していなければ-1を返す。
     * 
     * @param item 検索するアイテム
     * @return アイテムのIDか、-1
     */
    int getId(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        if (player != null) {
            Integer id = idCache.get(player);
            if (id != null) {
                return id;
            }
        }
        int id = database.query("SELECT id FROM " + TABLE + " WHERE uuid = '" + uniqueId + "' LIMIT 1", rs -> {
            try {
                return rs.next() ? rs.getInt("id") : -1;
            } catch (SQLException e) {
                return -1;
            }
        });

        if (id == -1) {
            id = insert(Bukkit.getOfflinePlayer(uniqueId));
        }

        return id;
    }

    int getId(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            Integer id = idCache.get(player);
            if (id != null) {
                return id;
            }
        }
        int id = database.query("SELECT id FROM " + TABLE + " WHERE name = '" + name + "' LIMIT 1", rs -> {
            try {
                return rs.next() ? rs.getInt("id") : -1;
            } catch (SQLException e) {
                return -1;
            }
        });

        if (id == -1) {
            @SuppressWarnings("deprecation")
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            id = insert(offlinePlayer);
        }

        return id;
    }

    UUID getUniqueId(int id) {
        Player player = idCache.inverse().get(id);
        if (player != null) {
            return player.getUniqueId();
        }
        return database.query("SELECT uuid FROM " + TABLE + " WHERE id = " + id + " LIMIT 1", rs -> {
            try {
                return rs.next() ? UUID.fromString(rs.getString("uuid")) : null;
            } catch (SQLException | IllegalArgumentException e) {
                return null;
            }
        });
    }

    UUID getUniqueId(String name) {
        return database.query("SELECT uuid FROM " + TABLE + " WHERE name = '" + name + "' LIMIT 1", rs -> {
            try {
                return rs.next() ? UUID.fromString(rs.getString("uuid")) : null;
            } catch (SQLException | IllegalArgumentException e) {
                return null;
            }
        });
    }

    String getName(int id) {
        Player player = idCache.inverse().get(id);
        if (player != null) {
            return player.getName();
        }
        return database.query("SELECT name FROM " + TABLE + " WHERE id = " + id + " LIMIT 1", rs -> {
            try {
                return rs.next() ? rs.getString("name") : null;
            } catch (SQLException | IllegalArgumentException e) {
                return null;
            }
        });
    }

    String getName(UUID uniqueId) {
        Player player = Bukkit.getPlayer(uniqueId);
        if (player != null) {
            return player.getName();
        }
        return database.query("SELECT name FROM " + TABLE + " WHERE uuid = '" + uniqueId + "' LIMIT 1", rs -> {
            try {
                return rs.next() ? rs.getString("name") : null;
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    boolean exists(int id) {
        return database.query("SELECT id FROM " + TABLE + " WHERE id = " + id + " LIMIT 1", rs -> {
            try {
                return rs.next();
            } catch (SQLException e) {
                return false;
            }
        });
    }

    boolean exists(UUID uniqueId) {
        return database.query("SELECT id FROM " + TABLE + " WHERE uuid = '" + uniqueId + "' LIMIT 1", rs -> {
            try {
                return rs.next();
            } catch (SQLException e) {
                return false;
            }
        });
    }

    boolean exists(String name) {
        return database.query("SELECT id FROM " + TABLE + " WHERE name = '" + name + "' LIMIT 1", rs -> {
            try {
                return rs.next();
            } catch (SQLException e) {
                return false;
            }
        });
    }

    List<OfflinePlayer> getAll() {
        return database.query("SELECT uuid FROM " + TABLE, rs -> {
            List<OfflinePlayer> offlinePlayers = new ArrayList<>();
            try {
                while (rs.next()) {
                    offlinePlayers.add(Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("uuid"))));
                }
            } catch (SQLException | IllegalArgumentException ignored) {
            }
            
            return offlinePlayers;
        });
    }

    List<String> getAllName() {
        return database.query("SELECT name FROM " + TABLE, rs -> {
            List<String> players = new ArrayList<>();
            try {
                while (rs.next()) {
                    players.add(rs.getString("name"));
                }
            } catch (SQLException | IllegalArgumentException ignored) {
            }
            
            return players;
        });
    }
}