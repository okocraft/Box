package net.okocraft.box.plugin.config;

import com.github.siroshun09.configapi.bukkit.BukkitConfig;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.database.connector.Database;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GeneralConfig extends BukkitConfig {

    private final Box plugin;

    public GeneralConfig(@NotNull Box plugin) {
        super(plugin, "config.yml", true);
        this.plugin = plugin;
    }

    @NotNull
    public Database.Type getDatabaseType() {
        String value = getString("database.type", "SQLite");
        try {
            return Database.Type.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid database type: " + value);
            plugin.getLogger().warning("Using SQLite...");
            return Database.Type.SQLITE;
        }
    }

    @NotNull
    public String getDatabaseAddress() {
        return getString("database.address", "localhost");
    }

    public int getDatabasePort() {
        return getInt("database.port", 3306);
    }

    @NotNull
    public String getDatabaseName() {
        return getString("database.name", "box_database");
    }

    public boolean isUsingSSL() {
        return getBoolean("database.use-ssl", false);
    }

    @NotNull
    public String getDatabaseUserName() {
        return getString("database.username", "root");
    }

    @NotNull
    public String getDatabasePassword() {
        return getString("database.password");
    }

    public int getMaxQuantity() {
        return getInt("max-quantity", 640);
    }

    @NotNull
    public List<String> getDisabledWorlds() {
        return getStringList("disabled-worlds");
    }

    public boolean isAutoStoreEnabled() {
        return getBoolean("auto-store", true);
    }

    public boolean isDebugMode() {
        return getBoolean("debug");
    }

    @NotNull
    public String getStickName() {
        return getString("stick.name", "&9Box Stick");
    }

    @NotNull
    public List<String> getStickLore() {
        return getStringList("stick.lore",
                List.of(
                        "&7",
                        "&7オフハンドに持つと",
                        "&7アイテムを消費した時に自動補充されます。",
                        "&7"
                ));
    }

    public boolean isStickGlowed() {
        return getBoolean("stick.glow");
    }
}
