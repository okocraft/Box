/*
プレイヤーデータテーブル
id | player | itemid | stock | autostore
1  | uuid1  | 1      | 1     | 0
2  | uuid2  | 1      | 1     | 0
3  | uuid3  | 1      | 1     | 0
4  | uuid4  | 1      | 1     | 0
5  | uuid5  | 1      | 1     | 0
...

ロードは非同期で行い、ロード前・中にはBoxにアクセス不可とする。
*/

package net.okocraft.box.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

/**
 * データを主に保存しているテーブル
 */
class PlayerDataTable {

    private static final String TABLE = "box_playerdata";

    private final Database database;
    private final ItemTable itemTable;

    PlayerDataTable(Database database, ItemTable itemTable) {
        this.database = database;
        this.itemTable = itemTable;
        database.execute("CREATE TABLE IF NOT EXISTS " + TABLE + " (id INTEGER PRIMARY KEY " + (database.isSQLite() ? "AUTOINCREMENT" : "AUTO_INCREMENT") + ", player CHAR(36) NOT NULL, itemid INTEGER NOT NULL, stock INTEGER NOT NULL DEFAULT 0, autostore INTEGER NOT NULL DEFAULT 0)");
        database.execute("CREATE INDEX IF NOT EXISTS playerindex ON " + TABLE + "(player, itemid)");
    }

    void setAutoStoreAll(OfflinePlayer player, boolean enabled) {
        int enabledInt = enabled ? 1 : 0;
        StringBuilder sb = new StringBuilder("UPDATE " + TABLE + " SET autostore = CASE itemid");
        itemTable.getAllId().forEach(itemId -> {
            sb.append(" WHEN ").append(itemId).append(" THEN ").append(enabledInt);
        });
        sb.append(" END WHERE player = '" + player.getUniqueId() + "'");

        database.execute(sb.toString());
    }

    void setAutoStoreAll(OfflinePlayer player, Map<ItemStack, Boolean> autostore) {
        if (autostore == null) {
            return;
        }
        StringBuilder sb = new StringBuilder("UPDATE " + TABLE + " SET autostore = CASE itemid");
        autostore.forEach((item, value) -> {
            if (item != null && value != null) {
                sb.append(" WHEN ").append(itemTable.getId(item)).append(" THEN ").append(value ? 1 : 0);
            }
        });
        sb.append(" END WHERE player = '" + player.getUniqueId() + "'");

        database.execute(sb.toString());
    }

    void setAutoStore(OfflinePlayer player, ItemStack item, boolean enabled) {
        int itemId = itemTable.register(item);
        int enabledInt = enabled ? 1 : 0;
        Boolean autostore = database.query("SELECT autostore FROM " + TABLE + " WHERE player = '" + player.getUniqueId() + "', itemid = " + itemId, rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt("autostore") == 1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });

        if (autostore == null) {
            database.execute("INSERT INTO " + TABLE + " (player, itemid, stock, autostore) VALUES ('" + player.getUniqueId() + "', " + itemId + ", 0, " + enabledInt + ")");
        } else if (enabled != autostore) {
            database.execute("UPDATE " + TABLE + " SET autostore = " + enabledInt + " WHERE player = '" + player.getUniqueId() + "', itemid = '" + itemId + "'");
        }
    }

    boolean getAutoStore(OfflinePlayer player, ItemStack item) {
        int itemId = itemTable.register(item);
        Boolean autostore = database.query("SELECT autostore FROM " + TABLE + " WHERE player = '" + player.getUniqueId() + "', itemid = " + itemId, rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt("autostore") == 1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });

        if (autostore != null) {
            return autostore;
        } else {
            database.execute("INSERT INTO " + TABLE + " (player, itemid, stock, autostore) VALUES ('" + player.getUniqueId() + "', " + itemId + ", 0, 0)");
            return false;
        }
    }

    Map<ItemStack, Boolean> getAutoStoreAll(OfflinePlayer player) {
        Map<ItemStack, Boolean> result = new HashMap<>();

        database.query("SELECT itemid, autostore FROM " + TABLE + " WHERE player = '" + player.getUniqueId() + "'", rs -> {
            try {
                while (rs.next()) {
                    result.put(itemTable.getItem(rs.getInt("itemid")), rs.getInt("autostore") == 1);
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
            return null;
        });

        itemTable.getAllItem().forEach(item -> {
            if (!result.containsKey(item)) {
                result.put(item, false);
            }
        });

        return result;
    }

    void setStock(OfflinePlayer player, ItemStack item, int stock) {
        int itemId = itemTable.register(item);
        Integer queryResult = database.query("SELECT stock FROM " + TABLE + " WHERE player = '" + player.getUniqueId() + "', itemid = " + itemId, rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt("stock");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });

        if (queryResult == null) {
            database.execute("INSERT INTO " + TABLE + " (player, itemid, stock, autostore) VALUES ('" + player.getUniqueId() + "', " + itemId + ", " + stock + ", 0)");
        } else if (queryResult != stock) {
            database.execute("UPDATE " + TABLE + " SET stock = " + stock + " WHERE player = '" + player.getUniqueId() + "', itemid = '" + itemId + "'");
        }
    }

    void setStockAll(OfflinePlayer player, Map<ItemStack, Integer> stock) {
        if (stock == null) {
            return;
        }
        StringBuilder sb = new StringBuilder("UPDATE " + TABLE + " SET stock = CASE itemid");
        stock.forEach((item, value) -> {
            if (item != null && value != null) {
                sb.append(" WHEN ").append(itemTable.getId(item)).append(" THEN ").append(value);
            }
        });
        sb.append(" END WHERE player = '" + player.getUniqueId() + "'");

        database.execute(sb.toString());
    }

    void addStock(OfflinePlayer player, ItemStack item, int amount) {
        setStock(player, item, getStock(player, item) + amount);
    }

    int getStock(OfflinePlayer player, ItemStack item) {
        int itemId = itemTable.register(item);
        Integer stock = database.query("SELECT stock FROM " + TABLE + " WHERE player = '" + player.getUniqueId() + "', itemid = " + itemId, rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt("stock");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });

        if (stock != null) {
            return stock;
        } else {
            database.execute("INSERT INTO " + TABLE + " (player, itemid, stock, autostore) VALUES ('" + player.getUniqueId() + "', " + itemId + ", 0, 0)");
            return 0;
        }
    }

    Map<ItemStack, Integer> getStockAll(OfflinePlayer player) {
        Map<ItemStack, Integer> result = new HashMap<>();

        database.query("SELECT itemid, stock FROM " + TABLE + " WHERE player = '" + player.getUniqueId() + "'", rs -> {
            try {
                while (rs.next()) {
                    result.put(itemTable.getItem(rs.getInt("itemid")), rs.getInt("stock"));
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
            return null;
        });

        itemTable.getAllItem().forEach(item -> {
            if (!result.containsKey(item)) {
                result.put(item, 0);
            }
        });

        return result;
    }
}