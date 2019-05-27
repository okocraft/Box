package net.okocraft.box;

import lombok.Getter;
import lombok.Setter;

import java.util.logging.Logger;

import net.okocraft.box.command.Commands;
import net.okocraft.box.command.BoxTabCompleter;
import net.okocraft.box.database.Database;
import net.okocraft.box.listeners.EntityPickupItem;
import net.okocraft.box.listeners.GuiManager;
import net.okocraft.box.listeners.PlayerJoin;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author OKOCRAFT
 */
public class Box extends JavaPlugin {

    /**
     * ロガー。
     */
    @Getter
    private final Logger log;

    /**
     * プラグイン Box のインスタンス。
     */
    private static Box instance;

    /**
     * バージョン。
     */
    @Getter
    private final String version;

    /**
     * データベース。
     */
    @Getter
    @Setter
    private Database database;

    /** コンフィグマネージャ */
    @Getter
    private ConfigManager configManager;

    /** GUIマネージャ */
    @Getter
    private GuiManager guiManager;

    public Box() {
        version = getClass().getPackage().getImplementationVersion();
        log = getLogger();
        database = new Database(this);
    }

    @Override
    public void onEnable() {
        // Initialize Config
        configManager = new ConfigManager(this, database);

        // Connect to database. If can't, disable Box.
        if (!database.connect(getDataFolder().getPath() + "/data.db")) {
            setEnabled(false);
            return;
        }

        // Implementation info
        log.info("Installed in : " + getDataFolder().getPath());
        log.info("Database file: " + database.getDBUrl());

        // registerEvents
        this.registerEvents();

        // Initialize Commandsc
        new Commands(database);
        new BoxTabCompleter(database);

        log.info("Box has been enabled!");
    }

    @Override
    public void onDisable() {
        database.dispose();
        HandlerList.unregisterAll(this);

        log.info("Box has been disabled!");
    }

    public static Box getInstance() {
        if (instance == null) {
            instance = (Box) Bukkit.getPluginManager().getPlugin("Box");
        }

        return instance;
    }
    public void registerEvents() {
        HandlerList.unregisterAll(this);
        new PlayerJoin(database, this);
        new EntityPickupItem(database, this);
        guiManager = new GuiManager(database, this);
        log.info("Events have been registered.");
    }
}
