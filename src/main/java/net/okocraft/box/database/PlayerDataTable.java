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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        database.execute("CREATE TABLE IF NOT EXISTS " + TABLE + " (id INTEGER PRIMARY KEY " + (database.isSQLite() ? "AUTOINCREMENT" : "AUTO_INCREMENT") + ", player CHAR(36) NOT NULL, itemid INTEGER NOT NULL, stock INTEGER NOT NULL DEFAULT 0, autostore INTEGER NOT NULL DEFAULT 0, UNIQUE(player, itemid))");
    }

    void setAutoStoreAll(OfflinePlayer player, boolean enabled) {
        if (enabled) {
            setAutoStoreAllTrue(player, itemTable.getAllItem());
        } else {
            database.execute("UPDATE " + TABLE + " SET autostore = 0 WHERE player = '" + player.getUniqueId() + "'");
        }
    }

    void setAutoStoreAll(OfflinePlayer player, Map<ItemStack, Boolean> autostore) {
        if (autostore == null) {
            return;
        }

        List<ItemStack> allTrue = new ArrayList<>();
        List<ItemStack> allFalse = new ArrayList<>();

        autostore.forEach((item, value) -> {
            if (value) {
                allTrue.add(item);
            } else {
                allFalse.add(item);
            }
        });

        setAutoStoreAllTrue(player, allTrue);
        setAutoStoreAllFlase(player, allFalse);
    }

    private void setAutoStoreAllTrue(OfflinePlayer player, Collection<ItemStack> items) {
        StringBuilder sb = new StringBuilder("INSERT INTO " + TABLE + " (player, itemid, stock, autostore) VALUES ");
        List<Integer> itemIds = items.stream().map(itemTable::getId).filter(id -> id != -1).collect(Collectors.toList());
        if (itemIds.isEmpty()) {
            return;
        }
        itemIds.forEach(itemId -> 
                sb.append(" ('").append(player.getUniqueId().toString()).append("', ")
                .append(itemId).append(", 0, ").append("1").append("),")
        );
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        if (database.isSQLite()) {
            sb.append(" ON CONFLICT (player, itemid) DO UPDATE SET autostore = 1");
        } else {
            sb.append(" ON DUPLICATE KEY UPDATE autostore = 1");
        }

        database.execute(sb.toString());
    }

    private void setAutoStoreAllFlase(OfflinePlayer player, Collection<ItemStack> items) {
        StringBuilder sb = new StringBuilder("UPDATE " + TABLE + " SET autostore = CASE itemid");
        StringBuilder where = new StringBuilder();
        items.forEach(item -> {
            if (item != null) {
                int itemId = itemTable.getId(item);
                sb.append(" WHEN ").append(itemId).append(" THEN ").append(0);
                where.append(itemId).append(", ");
            }
        });
        where.delete(where.length() - 3, where.length());
        sb.append(" END WHERE player = '").append(player.getUniqueId().toString()).append("' AND itemid IN (").append(where).append(")");

        database.execute(sb.toString());
    }

    void setAutoStore(OfflinePlayer player, ItemStack item, boolean enabled) {
        int itemId = itemTable.register(item);
        if (itemId == -1) {
            return;
        }
        int enabledInt = enabled ? 1 : 0;
        Boolean autostore = database.query("SELECT autostore FROM " + TABLE + " WHERE player = '" + player.getUniqueId() + "' AND itemid = " + itemId, rs -> {
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
            if (enabled) {
                database.execute("INSERT INTO " + TABLE + " (player, itemid, stock, autostore) VALUES ('" + player.getUniqueId() + "', " + itemId + ", 0, " + enabledInt + ")");
            }
        } else if (enabled != autostore) {
            database.execute("UPDATE " + TABLE + " SET autostore = " + enabledInt + " WHERE player = '" + player.getUniqueId() + "' AND itemid = '" + itemId + "'");
        }
    }

    boolean getAutoStore(OfflinePlayer player, ItemStack item) {
        int itemId = itemTable.register(item);
        if (itemId == -1) {
            return false;
        }
        return database.query("SELECT autostore FROM " + TABLE + " WHERE player = '" + player.getUniqueId() + "' AND itemid = " + itemId, rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt("autostore") == 1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
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
        if (itemId == -1) {
            return;
        }

        Map<ItemStack, Integer> map = new HashMap<>();
        map.put(item, stock);
        setStockAll(player, map);
    }

    void setStockAll(OfflinePlayer player, Map<ItemStack, Integer> stock) {
        if (stock == null) {
            return;
        }
        List<ItemStack> stockTo0 = new ArrayList<>();
        Map<ItemStack, Integer> stockToNot0 = new HashMap<>();
        stock.forEach((item, value) -> {
            if (itemTable.getId(item) == -1) {
                if (value == 0) {
                    stockTo0.add(item);
                } else if (value != null) {
                    stockToNot0.put(item, value);
                }
            }
        });

        if (!stockTo0.isEmpty()) {
            setStockAll0(player, stockTo0);
        }
        if (!stockToNot0.isEmpty()) {

            setStockAllNot0(player, stockToNot0);
        }
    }

    private void setStockAll0(OfflinePlayer player, List<ItemStack> stockTo0) {
        StringBuilder sb = new StringBuilder("UPDATE " + TABLE + " SET stock = CASE itemid");
        StringBuilder where = new StringBuilder();
        stockTo0.forEach(item -> {
            if (item != null) {
                int itemId = itemTable.getId(item);
                sb.append(" WHEN ").append(itemId).append(" THEN ").append(0);
                where.append(itemId).append(", ");
            }
        });
        where.delete(where.length() - 3, where.length());
        sb.append(" END WHERE player = '").append(player.getUniqueId().toString()).append("' AND itemid IN (").append(where).append(")");

        database.execute(sb.toString());
    }

    private void setStockAllNot0(OfflinePlayer player, Map<ItemStack, Integer> stock) {
        StringBuilder sb = new StringBuilder("INSERT INTO " + TABLE + " (player, itemid, stock, autostore) VALUES ");
        stock.forEach((item, value) ->  sb.append(" ('")
                .append(player.getUniqueId().toString()).append("', ")
                .append(itemTable.getId(item)).append(", ")
                .append(Objects.requireNonNullElse(value, 0)).append(", ")
                .append("0").append("),")
        );
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        if (database.isSQLite()) {
            sb.append(" ON CONFLICT (player, itemid) DO UPDATE SET stock = excluded.stock");
        } else {
            sb.append(" ON DUPLICATE KEY UPDATE stock = VALUES(stock)");
        }

        database.execute(sb.toString());
    }

    void addStock(OfflinePlayer player, ItemStack item, int amount) {
        setStock(player, item, getStock(player, item) + amount);
    }

    int getStock(OfflinePlayer player, ItemStack item) {
        int itemId = itemTable.register(item);
        if (itemId == -1) {
            return 0;
        }
        return database.query("SELECT stock FROM " + TABLE + " WHERE player = '" + player.getUniqueId() + "' AND itemid = " + itemId, rs -> {
            try {
                if (rs.next()) {
                    return rs.getInt("stock");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
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