package net.okocraft.box.migrator;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.migrator.database.Database;
import net.okocraft.box.migrator.database.MySQLDatabase;
import net.okocraft.box.migrator.database.SQLiteDatabase;
import net.okocraft.box.migrator.table.ItemTable;
import net.okocraft.box.migrator.table.MasterTable;
import net.okocraft.box.migrator.table.PlayerTable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BoxMigrator extends JavaPlugin {

    private Boolean isSQLite;
    private boolean migrated;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("This command can only be executed from the console.");
            return true;
        }

        if (migrated) {
            sender.sendMessage("Already migrated!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("/boxmigrate <sqlite/mysql>");
            return true;
        }

        if (isSQLite != null && args[0].equalsIgnoreCase("confirm")) {
            migrated = true;
            CompletableFuture.runAsync(() -> {
                if (isSQLite) {
                    sender.sendMessage("Starting migrate from SQLite...");

                    if (processSQLite()) {
                        sender.sendMessage("Migrated from SQLite!");
                    }
                } else {
                    sender.sendMessage("Starting migrate from MySQL...");

                    if (processMySQL()) {
                        sender.sendMessage("Migrated from MySQL!");
                    }
                }
                sender.sendMessage("Please stop the server and delete BoxMigrator-x.x.x.jar.");
            }).exceptionallyAsync(throwable -> {
                getLogger().log(Level.SEVERE, "Could not complete migration", throwable);
                return null;
            });

            return true;
        }

        if (args[0].equalsIgnoreCase("sqlite")) {
            sender.sendMessage("Start the migration from SQLite (database.db).");
            sender.sendMessage("The stock data to be migrated will be added to the existing stock data.");
            sender.sendMessage("The AutoStore settings will not be migrated.");
            sender.sendMessage("");
            sender.sendMessage("Please be sure to check the following pages as well.");
            sender.sendMessage("https://github.com/okocraft/Box/wiki/migration-from-v3");
            sender.sendMessage("");
            sender.sendMessage("To start the migration, run /boxmigrate confirm.");

            isSQLite = true;
            return true;
        }

        if (args[0].equalsIgnoreCase("mysql")) {
            sender.sendMessage("Start the migration from MySQL.");
            sender.sendMessage("The stock data to be migrated will be added to the existing stock data.");
            sender.sendMessage("The AutoStore settings will not be migrated.");
            sender.sendMessage("");
            sender.sendMessage("To migrate from MySQL, please follow the steps below.");
            sender.sendMessage("1. create ./plugins/Box/mysql.yml");
            sender.sendMessage(
                    """
                            2. Write the following Yaml settings
                            database:
                              mysql-settings:
                                host: "<localhost or database address>"
                                port: "<database port, default 3306>"
                                user: "<username>"
                                password: "<password>"
                                db-name: "<database-name>\""""
            );
            sender.sendMessage("This is the same structure as the previous config.yml.");
            sender.sendMessage("");
            sender.sendMessage("Please be sure to check the following pages as well.");
            sender.sendMessage("https://github.com/okocraft/Box/wiki/migration-from-v3");
            sender.sendMessage("");
            sender.sendMessage("3. run /boxmigrate confirm");

            isSQLite = false;
            return true;
        }

        sender.sendMessage("/boxmigrate <sqlite/mysql>");
        return true;
    }

    private boolean processSQLite() {
        var databasePath = BoxProvider.get().getPluginDirectory().resolve("database.db");

        if (Files.isRegularFile(databasePath)) {
            var database = new SQLiteDatabase(databasePath);
            migrate(database);
            database.close();
            return true;
        } else {
            getLogger().warning("./plugins/Box/database.db does not exist!");
            return false;
        }
    }

    private boolean processMySQL() {
        var configPath = BoxProvider.get().getPluginDirectory().resolve("mysql.yml");

        if (Files.isRegularFile(configPath)) {
            getLogger().warning("mysql.yml not found!");
            return false;
        }

        Database database;

        try (var config = YamlConfiguration.create(configPath)) {
            config.load();

            var section = config.getSection("database.mysql-settings");

            if (section == null) {
                getLogger().warning("Could not found 'database.mysql-setting' in mysql.yml");
                return false;
            }

            database = new MySQLDatabase(
                    section.getString("host"),
                    section.getInteger("port"),
                    section.getString("user"),
                    section.getString("password"),
                    section.getString("db-name")
            );
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load mysql.yml", e);
            return false;
        }

        migrate(database);
        database.close();
        return true;
    }

    private void migrate(@NotNull Database database) {
        var playerMap = new PlayerTable(database).load();
        var itemIdMap = new ItemTable(database).loadAndMigrate();

        var masterTable = new MasterTable(database, itemIdMap);

        for (var entry : playerMap.entrySet()) {
            masterTable.migrate(entry.getKey(), entry.getValue());
        }
    }
}
