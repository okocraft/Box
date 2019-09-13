package net.okocraft.box.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.okocraft.box.Box;

public class PlayerData implements Listener {
    private static final Map<Player, Map<String, Boolean>> autoStoreData = new HashMap<>();
    private static final Map<Player, Map<String, Long>> itemData = new HashMap<>();

    private static Connection connection = Sqlite.getConnection();
    private static final String autoStoreTableName = "autostore_data";
    private static final String itemTableName = "item_data";

    private static final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    static {
        Sqlite.executeSql("CREATE TABLE IF NOT EXISTS " + autoStoreTableName + " (uuid TEXT PRIMARY KEY NOT NULL, player TEXT NOT NULL)");
        Sqlite.executeSql("CREATE TABLE IF NOT EXISTS " + itemTableName + " (uuid TEXT PRIMARY KEY NOT NULL, player TEXT NOT NULL)");
        syncDatabaseItems();
    }

    public PlayerData(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        new BukkitRunnable() {
            @Override
            public void run() {
                saveOnlinePlayersData();
            }
        }.runTaskTimerAsynchronously(Box.getInstance(), 20 * 60 * 15, 20 * 60 * 15);
    }

    public static void setAutoStoreAll(OfflinePlayer player, boolean enabled) {
        if (player.isOnline()) {
            getPlayerAutoStoreData(player).replaceAll((item, current) -> enabled);
        } else if (player.hasPlayedBefore()) {
            StringBuilder values = new StringBuilder(
                    "'" + player.getUniqueId().toString() + "', '" + player.getName().toLowerCase() + "', ");
            String enabledStr = enabled ? "1, " : "0, ";
            for (int i = 1; i <= Items.values().length; i++) {
                values.append(enabledStr);
            }
            values = values.delete(values.length() - 2, values.length());

            Sqlite.executeSql("REPLACE INTO " + autoStoreTableName + " VALUES (" + values.toString() + ")");
        }
    }

    public static void setAutoStore(OfflinePlayer player, Items item, boolean enabled) {
        if (player.isOnline()) {
            getPlayerAutoStoreData(player).put(item.name(), enabled);
        } else if (player.hasPlayedBefore()) {
            Sqlite.executeSql("UPDATE OR IGNORE " + autoStoreTableName + " SET " + item.name() + " = "
                    + (enabled ? "1" : "0") + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
        }
    }

    public static boolean getAutoStore(OfflinePlayer player, Items item) {
        return getPlayerAutoStoreData(player).get(item.name());
    }

    public static Map<String, Boolean> getAutoStoreAll(OfflinePlayer player) {
        return new LinkedHashMap<>(getPlayerAutoStoreData(player));
    }

    public static void setItemAmount(OfflinePlayer player, Items item, long amount) {
        if (player.isOnline()) {
            getPlayerItemData(player).put(item.name(), amount);
        } else {
            Sqlite.executeSql("UPDATE OR IGNORE " + itemTableName + " SET " + item.name() + " = " + amount
                    + " WHERE uuid = '" + player.getUniqueId().toString() + "'");
        }
    }

    public static long getItemAmount(OfflinePlayer player, Items item) {
        String itemName = item.name();
        return getPlayerItemData(player).get(itemName);
    }

    public static Map<String, Long> getItemsAmount(OfflinePlayer player) {
        return new LinkedHashMap<>(getPlayerItemData(player));
    }

    public static Map<String, String> getPlayers() {
        Map<String, String> result = new HashMap<>();
        try (PreparedStatement sql = connection.prepareStatement("SELECT uuid, player FROM " + autoStoreTableName)) {
            ResultSet rs = sql.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("uuid"), rs.getString("player"));
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String name = player.getName().toLowerCase();

        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                // 昔同じ名前のプレイヤーがログインしてた場合、古い方を殲滅する。
                Sqlite.executeSql(
                        "UPDATE OR IGNORE " + autoStoreTableName + " SET player = '' WHERE player = '" + name + "'");
                Sqlite.executeSql(
                        "UPDATE OR IGNORE " + autoStoreTableName + " SET player = '' WHERE uuid = '" + uuid + "'");
                Sqlite.executeSql(
                        "UPDATE OR IGNORE " + itemTableName + " SET player = '' WHERE player = '" + name + "'");
                Sqlite.executeSql("UPDATE OR IGNORE " + itemTableName + " SET player = '' WHERE uuid = '" + uuid + "'");
                // 新しいプレイヤーだったら登録し、すでにuuidが登録されている場合はプレイヤーの名前だけ上書きする。
                Sqlite.executeSql("INSERT INTO " + autoStoreTableName + " (uuid, player) VALUES ('" + uuid + "', '"
                        + name + "') ON CONFLICT(uuid) DO UPDATE SET player = '" + name + "' WHERE uuid = '" + uuid
                        + "'");
                Sqlite.executeSql("INSERT INTO " + itemTableName + " (uuid, player) VALUES ('" + uuid + "', '" + name
                        + "') ON CONFLICT(uuid) DO UPDATE SET player = '" + name + "' WHERE uuid = '" + uuid + "'");
                // データを拾う。
                itemData.put(player, loadItemData((OfflinePlayer) player));
                autoStoreData.put(player, loadAutoStoreData((OfflinePlayer) player));
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                saveItemData(player);
                saveAutoStoreData(player);
            }
        });
    }

    public static void loadOnlinePlayersData() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            getPlayerAutoStoreData(player);
            getPlayerItemData(player);
        });
    }

    private static Map<String, Boolean> getPlayerAutoStoreData(OfflinePlayer player) {
        if (player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            Map<String, Boolean> data = autoStoreData.get(onlinePlayer);
            if (data == null) {
                autoStoreData.put(onlinePlayer, loadAutoStoreData(player));
                data = autoStoreData.get(player);
            }

            return data;
        } else {
            return loadAutoStoreData(player);
        }
    }

    private static Map<String, Long> getPlayerItemData(OfflinePlayer player) {
        if (player.isOnline()) {
            Player onlinePlayer = player.getPlayer();

            Map<String, Long> data = itemData.get(player);
            if (data == null) {
                itemData.put(onlinePlayer, loadItemData(player));
                data = itemData.get(onlinePlayer);
            }

            return data;
        } else {
            return loadItemData(player);
        }
    }

    private static Map<String, Long> loadItemData(OfflinePlayer player) {
        String sqlState = "SELECT * FROM " + itemTableName + " WHERE uuid = '" + player.getUniqueId().toString() + "'";
        try (PreparedStatement statement = connection.prepareStatement(sqlState)) {
            ResultSet rs = statement.executeQuery();
            Map<String, Long> result = new LinkedHashMap<>();
            rs.next();

            for (Items item : Items.values()) {
                String itemName = item.name();
                result.put(itemName, rs.getLong(itemName));
            }

            return result;
        } catch (SQLException e) {
            return Arrays.stream(Items.values()).map(Items::name)
                    .collect(Collectors.toMap(Function.identity(), item -> 0L, (e1, e2) -> e1, LinkedHashMap::new));
        }
    }

    private static Map<String, Boolean> loadAutoStoreData(OfflinePlayer player) {
        String sqlState = "SELECT * FROM " + autoStoreTableName + " WHERE uuid = '" + player.getUniqueId().toString() + "'";
        try (PreparedStatement statement = connection.prepareStatement(sqlState)) {
            ResultSet rs = statement.executeQuery();
            Map<String, Boolean> result = new LinkedHashMap<>();
            rs.next();

            for (Items item : Items.values()) {
                String itemName = item.name();
                result.put(itemName, rs.getLong(itemName) == 1);
            }

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return Arrays.stream(Items.values()).map(Items::name)
                    .collect(Collectors.toMap(Function.identity(), item -> false, (e1, e2) -> e1, LinkedHashMap::new));
        }
    }

    public static void saveOnlinePlayersData() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            saveItemData(player);
            saveAutoStoreData(player);
        });
    }

    private static void saveItemData(Player player) {
        String uuid = player.getUniqueId().toString();
        Map<String, Long> playerItemData = itemData.get(player);
        if (playerItemData == null) {
            return;
        }
        StringBuilder columns = new StringBuilder(" (uuid, player, ");
        StringBuilder values = new StringBuilder(" VALUES ('").append(uuid).append("', '")
                .append(player.getName().toLowerCase()).append("', ");

        playerItemData.forEach((column, value) -> {
            columns.append(column).append(", ");
            values.append(value).append(", ");
        });
        columns.delete(columns.length() - 2, columns.length()).append(")");
        values.delete(values.length() - 2, values.length()).append(")");

        Sqlite.executeSql("REPLACE INTO " + itemTableName + columns.toString() + values.toString());
    }

    private static void saveAutoStoreData(Player player) {
        String uuid = player.getUniqueId().toString();
        Map<String, Boolean> playerAutoStoreData = autoStoreData.get(player);
        if (playerAutoStoreData == null) {
            return;
        }

        StringBuilder columns = new StringBuilder(" (uuid, player, ");
        StringBuilder values = new StringBuilder(" VALUES ('").append(uuid).append("', '")
                .append(player.getName().toLowerCase()).append("', ");

        playerAutoStoreData.forEach((column, value) -> {
            columns.append(column).append(", ");
            values.append(value ? "1" : "0").append(", ");
        });
        columns.delete(columns.length() - 2, columns.length()).append(")");
        values.delete(values.length() - 2, values.length()).append(")");

        Sqlite.executeSql("REPLACE INTO " + autoStoreTableName + columns.toString() + values.toString());
    }

    private static void syncDatabaseItems() {
        Set<String> columns;
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + autoStoreTableName + " WHERE 0 = 1")) {
            ResultSetMetaData rsMeta = statement.executeQuery().getMetaData();

            columns = new HashSet<>();
            for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
                columns.add(rsMeta.getColumnName(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (columns.isEmpty()) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.addBatch("BEGIN TRANSACTION");
            statement.addBatch("ALTER TABLE " + autoStoreTableName + " RENAME TO temp_" + autoStoreTableName + "");
            statement.addBatch("ALTER TABLE " + itemTableName + " RENAME TO temp_" + itemTableName + "");

            StringBuilder stateBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS %table% (uuid TEXT PRIMARY KEY NOT NULL, player TEXT NOT NULL, ");
            StringBuilder itemsName = new StringBuilder();

            Arrays.stream(Items.values()).map(Items::name).forEach(item -> {
                if (columns.contains(item)) {
                    itemsName.append(item).append(", ");
                }
                stateBuilder.append(item).append(" INTEGER NOT NULL DEFAULT %default_value%, ");
            });
            
            String initState = stateBuilder.delete(stateBuilder.length() - 2, stateBuilder.length()).append(")").toString();
            int defaultAutoStore = Box.getInstance().getGeneralConfig().isAutoStoreEnabledByDefault() ? 1 : 0;

            statement.addBatch(initState.replace("%table%", autoStoreTableName).replace("%default_value%", Integer.toString(defaultAutoStore)));
            statement.addBatch(initState.replace("%table%", itemTableName).replace("%default_value%", "0"));
            
            if (itemsName.length() > 2) {
                itemsName.delete(itemsName.length() - 2, itemsName.length());
                statement.addBatch("INSERT INTO " + autoStoreTableName + " (uuid, player, " + itemsName.toString() + ") SELECT uuid, player, " + itemsName.toString() + " FROM temp_" + autoStoreTableName);
                statement.addBatch("INSERT INTO " + itemTableName + " (uuid, player, " + itemsName.toString() + ") SELECT uuid, player, " + itemsName.toString() + " FROM temp_" + itemTableName);    
            }

            statement.addBatch("DROP TABLE temp_" + autoStoreTableName);
            statement.addBatch("DROP TABLE temp_" + itemTableName);
            statement.addBatch("COMMIT");
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }
}