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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("This command can only be executed from the console.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("/boxmigrate <sqlite/mysql>");
            return true;
        }

        if (isSQLite != null && args[0].equalsIgnoreCase("confirm")) {
            CompletableFuture.runAsync(() -> {
                if (isSQLite) {
                    sender.sendMessage("Starting migrate from SQLite...");
                    processSQLite();
                    sender.sendMessage("Migrated from SQLite!");
                } else {
                    sender.sendMessage("Starting migrate from MySQL...");
                    processMySQL();
                    sender.sendMessage("Migrated from MySQL!");
                }
            });

            return true;
        }

        if (args[0].equalsIgnoreCase("sqlite")) {
            sender.sendMessage("Start the migration from SQLite (database.db).");
            sender.sendMessage("The stock data to be migrated will be added to the existing stock data.");
            sender.sendMessage("The AutoStore settings will not be migrated.");
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
            sender.sendMessage("2. Write the following Yaml settings.");
            sender.sendMessage(
                    """
                            database:
                              mysql-settings:
                                host: "<localhost or database address>"
                                port: "<database port, default 3306>"
                                user: "<username>"
                                password: "<password>"
                                db-name: "<database-name>"
                            """
            );
            sender.sendMessage("This is the same structure as the previous config.yml.");
            sender.sendMessage("3. run /boxmigrate confirm");

            isSQLite = false;
            return true;
        }

        return true;
    }

    private void processSQLite() {
        var database = new SQLiteDatabase(BoxProvider.get().getPluginDirectory().resolve("database.db"));
        migrate(database);
        database.close();
    }

    private void processMySQL() {
        var configPath = BoxProvider.get().getPluginDirectory().resolve("mysql.yml");

        if (Files.isRegularFile(configPath)) {
            getLogger().warning("mysql.yml not found!");
            return;
        }

        Database database;

        try (var config = YamlConfiguration.create(configPath)) {
            config.load();

            var section = config.getSection("database.mysql-settings");

            if (section == null) {
                getLogger().warning("Could not found 'database.mysql-setting' in mysql.yml");
                return;
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
            return;
        }

        migrate(database);
        database.close();
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
