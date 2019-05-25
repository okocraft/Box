package net.okocraft.box.command;

import net.okocraft.box.ConfigManager;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

@SuppressWarnings("unused")
public class Commands {
    private Database database;
    private Box instance;
    private ConfigManager configManager;
    private FileConfiguration messageConfig;

    public Commands(Database database) {
        this.database = database;
        this.instance = Box.getInstance();
        this.configManager = Box.getInstance().getConfigManager();
        this.messageConfig = configManager.getMessageConfig();
        // Register command /box
        instance.getCommand("box").setExecutor(new BoxCommand(this.database));
        instance.getCommand("boxadmin").setExecutor(new BoxAdminCommand(this.database));
    }

    public static String checkEntryType(String entry) {
        return entry.matches("([a-z]|\\d){8}(-([a-z]|\\d){4}){3}-([a-z]|\\d){12}") ? "uuid" : "player";
    }

    /**
     * 権限がないときにメッセージを送りつつfalseを返す。
     *
     * @param sender
     * @param permission
     * @return 権限がないときにfalse あればtrue
     */
    public static boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission))
            return errorOccured(sender, ":PERM_INSUFFICIENT_" + permission);
        return true;
    }

    /**
     * エラーが発生したときにメッセージを送りつつfalseを返す。
     *
     * @param sender
     * @param errorMessage
     * @return false
     */
    public static boolean errorOccured(CommandSender sender, String errorMessage) {
        sender.sendMessage(errorMessage);
        return false;
    }
}
