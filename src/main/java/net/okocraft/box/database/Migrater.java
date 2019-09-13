package net.okocraft.box.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import net.okocraft.box.Box;

public class Migrater {

    private static final Box box = Box.getInstance();
    private static Connection connection;

    private Migrater() {
    }

    private static Connection getConnection() {
        if (connection == null) {
            connection = connect();
        }
        return connection;
    }

    private static Connection connect() {
        if (!existJDBC()) {
            box.getLogger().severe("There's no JDBC driver. Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(box);
            return null;
        }

        final File dbFile = box.getDataFolder().toPath().resolve("data_old.db").toFile();
        if (!dbFile.exists()) {
            try {
                Files.createDirectories(box.getDataFolder().toPath());
                dbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(box);
                return null;
            }
        }

        try {
            return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(box);
            return null;
        }
    }

    private static boolean disconnect() {
        if (connection != null) {

            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
                connection = null;
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
        }

    }

    private static boolean existJDBC() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void migrate() {
        Executors.newSingleThreadExecutor().submit(new Runnable() {

            @Override
            public void run() {

                getOldDBPlayers().forEach((uuid, player) -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                    registerPlayer(uuid, player);

                    try (PreparedStatement statement = connection
                            .prepareStatement("SELECT * FROM Box WHERE uuid = '" + uuid + "'")) {
                        for (Items item : Items.values()) {
                            ResultSet rs = statement.executeQuery();
                            long amount;
                            boolean autoStore;
                            try {
                                amount = Long.parseLong(rs.getString(item.name()));
                                autoStore = rs.getString("autostore_" + item.name()).equalsIgnoreCase("true");
                            } catch (SQLException | NumberFormatException e) {
                                continue;
                            }

                            PlayerData.setItemAmount(offlinePlayer, item, amount);
                            PlayerData.setAutoStore(offlinePlayer, item, autoStore);

                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return;
                    }
                });

                System.out.println("移行完了");
                disconnect();
            }
        });
    }

    private static void registerPlayer(String uuid, String name) {
        Sqlite.executeSql("INSERT OR IGNORE INTO autostore_data (uuid, player) VALUES ('" + uuid + "', '" + name
                + "') ON CONFLICT(uuid) DO UPDATE SET player = '" + name + "' WHERE uuid = '" + uuid + "'");
        Sqlite.executeSql("INSERT OR IGNORE INTO item_data (uuid, player) VALUES ('" + uuid + "', '" + name
                + "') ON CONFLICT(uuid) DO UPDATE SET player = '" + name + "' WHERE uuid = '" + uuid + "'");
    }

    private static Map<String, String> getOldDBPlayers() {
        Map<String, String> result = new HashMap<>();
        try (PreparedStatement sql = getConnection().prepareStatement("SELECT uuid, player FROM Box")) {
            ResultSet rs = sql.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("uuid"), rs.getString("player"));
            }
            return result;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return Map.of();
        }
    }
}