package net.okocraft.box.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import net.okocraft.box.Box;
import org.jetbrains.annotations.Nullable;

public final class Sqlite {

    @Nullable
    private static final Box box = Box.getInstance();
    @Nullable
    private static Connection connection;

    private Sqlite() {
    }

    @Nullable
    static Connection getConnection() {
        if (connection == null) {
            connection = connect();
        }
        return connection;
    }

    @Nullable
    private static Connection connect() {
        if (!existJDBC()) {
            box.getLogger().severe("There's no JDBC driver. Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(box);
            return null;
        }

        final File dbFile = box.getDataFolder().toPath().resolve("data.db").toFile();
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

    public static boolean disconnect() {
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

    static boolean executeSql(String sqlState) {
        try (PreparedStatement sql = connection.prepareStatement(sqlState)) {
            sql.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
}