package net.okocraft.box.command;

import lombok.val;
import net.okocraft.box.ConfigManager;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Optional;

@SuppressWarnings("unused")
public class Commands {
    private Database database;

    public Commands(Database database) {
        this.database = database;

        val instance = Box.getInstance();
        val config   = instance.getConfigManager();
        val messageConfig = config.getMessageConfig();

        // CHANGED: JavaPlugin#getCommand() は Nullable っぽいので Optional 化
        // Register command /box
        Optional.ofNullable(instance.getCommand("box")).ifPresent(cmd ->
                cmd.setExecutor(new BoxCommand(this.database))
        );

        Optional.ofNullable(instance.getCommand("boxadmin")).ifPresent(cmd ->
                cmd.setExecutor(new BoxAdminCommand(this.database))
        );
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
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(":PERM_INSUFFICIENT_" + permission);

            return false;
        }

        return true;
    }
}
