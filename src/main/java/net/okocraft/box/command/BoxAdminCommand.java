package net.okocraft.box.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.okocraft.box.ConfigManager;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

import lombok.NonNull;
import lombok.val;

public class BoxAdminCommand implements CommandExecutor {

    private Database database;
    private Box instance;
    private ConfigManager configManager;
    private FileConfiguration messageConfig;
    private String noEnoughArguments;

    protected BoxAdminCommand(Database database) {
        this.database = database;
        instance = Box.getInstance();
        configManager = instance.getConfigManager();
        messageConfig = configManager.getMessageConfig();
        noEnoughArguments = messageConfig.getString("NoEnoughArgument", "&c引数が足りません。").replaceAll("&([a-f0-9])", "§$1");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // only /boxadmin
        if (args.length == 0) {
            return false;
        }

        @NonNull
        val subCommand = args[0];

        // test
        if (subCommand.equalsIgnoreCase("test")) {
            // Do stuff
        }

        // Box Database
        if (subCommand.equalsIgnoreCase("database")) {
            if (args.length == 1)
                return Commands.errorOccured(sender, noEnoughArguments);

            val databaseSubCommand = args[1].toLowerCase();
            if (databaseSubCommand.equalsIgnoreCase("addplayer")) {
                if (args.length == 2)
                    return Commands.errorOccured(sender, messageConfig.getString("NoEnoughArgument", "&c引数が足りません。")
                            .replaceAll("&([a-f0-9])", "§$1"));

                @SuppressWarnings("deprecation")
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);
                if (!player.hasPlayedBefore())
                    return Commands.errorOccured(sender, messageConfig.getString("NoPlayerFound", "&cプレイヤーが見つかりませんでした。")
                            .replaceAll("&([a-f0-9])", "§$1"));
                database.addPlayer(player.getUniqueId().toString(), player.getName(), true);
                return true;
            }
            if (databaseSubCommand.equalsIgnoreCase("removeplayer")) {
                if (args.length == 2)
                    return Commands.errorOccured(sender, messageConfig.getString("NoEnoughArgument", "&c引数が足りません。")
                            .replaceAll("&([a-f0-9])", "§$1"));

                database.removePlayer(args[2]);
                return true;
            }
            if (databaseSubCommand.equalsIgnoreCase("existplayer")) {
                if (args.length == 2)
                    return Commands.errorOccured(sender, messageConfig.getString("NoEnoughArgument", "&c引数が足りません。")
                            .replaceAll("&([a-f0-9])", "§$1"));

                sender.sendMessage(String.valueOf(database.existPlayer(args[2])));
                return true;
            }
            if (databaseSubCommand.equalsIgnoreCase("set")) {
                if (args.length < 4)
                    return Commands.errorOccured(sender, messageConfig.getString("NoEnoughArgument", "&c引数が足りません。")
                            .replaceAll("&([a-f0-9])", "§$1"));

                database.set(args[2], args[3], args[4]);
                return true;
            }
            if (databaseSubCommand.equalsIgnoreCase("get")) {
                if (args.length < 3)
                    return Commands.errorOccured(sender, messageConfig.getString("NoEnoughArgument", "&c引数が足りません。")
                            .replaceAll("&([a-f0-9])", "§$1"));

                sender.sendMessage(database.get(args[2], args[3]));
                return true;
            }
            if (databaseSubCommand.equalsIgnoreCase("addcolumn")) {
                if (args.length < 4)
                    return Commands.errorOccured(sender, messageConfig.getString("NoEnoughArgument", "&c引数が足りません。")
                            .replaceAll("&([a-f0-9])", "§$1"));

                if (args[4].equalsIgnoreCase("null"))
                    args[4] = null;
                database.addColumn(args[2], args[3], args[4], true);
                return true;
            }
            if (databaseSubCommand.equalsIgnoreCase("dropcolumn")) {
                if (args.length == 2)
                    return Commands.errorOccured(sender, messageConfig.getString("NoEnoughArgument", "&c引数が足りません。")
                            .replaceAll("&([a-f0-9])", "§$1"));

                database.dropColumn(args[2]);
                return true;
            }
            if (databaseSubCommand.equalsIgnoreCase("getcolumnmap")) {
                sender.sendMessage("列リスト");
                database.getColumnMap().forEach((columnName, columnType) -> {
                    sender.sendMessage(columnName + " - " + columnType);
                });
                ;
                return true;
            }
            if (databaseSubCommand.equalsIgnoreCase("getplayersmap")) {
                sender.sendMessage("記録されているプレイヤー");
                database.getPlayersMap().forEach((uuidStr, name) -> {
                    sender.sendMessage(uuidStr + " - " + name);
                });
                ;
                return true;
            }
            return Commands.errorOccured(sender,
                    messageConfig.getString("NoParamExist", "&c指定された引数は存在しません。").replaceAll("&([a-f0-9])", "§$1"));
        }

        // Box migrate
        if (subCommand.equalsIgnoreCase("migrate")) {
            Map<String, String> scoreItemMap = new HashMap<>();
            ((MemorySection) configManager.getDefaultConfig().get("Migration.Combination")).getValues(false)
                    .forEach((k, v) -> {
                        scoreItemMap.put(k, (String) v);
                    });
            scoreItemMap.forEach((k, v) -> {
                Material.valueOf(k.toUpperCase());
            });

            return true;
        }

        // Box reload
        if (subCommand.equalsIgnoreCase("reload")) {
            configManager.reloadConfig();
            sender.sendMessage(
                    messageConfig.getString("ConfigReloaded", "&7設定を再読込しました。").replaceAll("&([a-f0-9])", "§$1"));
            return true;
        }

        // for /boxadmin set|give|take player item [amount]
        if (subCommand.equalsIgnoreCase("set") || subCommand.equalsIgnoreCase("give")
                || subCommand.equalsIgnoreCase("take")) {
            if (args.length < 3) {
                return Commands.errorOccured(sender, noEnoughArguments);
            }

            val playerName = args[1];
            val itemName = args[2].toUpperCase();

            if (!database.getPlayersMap().values().contains(args[1])
                    && !database.getPlayersMap().keySet().contains(args[1]))
                return Commands.errorOccured(sender, messageConfig.getString("NoPlayerFound", "&cその名前のプレイヤーは登録されていません。")
                        .replaceAll("&([a-f0-9])", "§$1"));

            if (!configManager.getAllItems().contains(itemName)) {
                return Commands.errorOccured(sender, messageConfig.getString("NoItemFound", "&cその名前のアイテムは登録されていません。")
                        .replaceAll("&([a-f0-9])", "§$1"));
            }

            Long amount = Longs.tryParse(args[3]);

            if (amount == null) {
                return Commands.errorOccured(sender, messageConfig.getString("InvalidNumberFormat", "&c数字のフォーマットが不正です。")
                        .replaceAll("&([a-f0-9])", "§$1"));
            }

            if (subCommand.equalsIgnoreCase("set")) {
                database.set(itemName, playerName, String.valueOf(amount));
                sender.sendMessage(
                        messageConfig.getString("SuccessfullySet", "&e%player%のボックスの%item%を%amount%にセットしました。")
                                .replaceAll("&([a-f0-9])", "§$1").replaceAll("%player%", playerName)
                                .replaceAll("%item%", itemName).replaceAll("%amount%", args[3]));
                return true;
            }

            Long currentAmount = Longs.tryParse(database.get(itemName, playerName));
            if (currentAmount == null) {
                return Commands.errorOccured(sender,
                        messageConfig.getString("DatabaseInvalidNumberFormat", "&c不正な数字が記録されています。")
                                .replaceAll("&([a-f0-9])", "§$1"));
            }
            if (subCommand.equalsIgnoreCase("give")) {
                database.set(itemName, playerName, String.valueOf(currentAmount + amount));
                sender.sendMessage(messageConfig
                        .getString("SuccessfullyGive", "&e%player%のボックスの%item%を%amount%増やしました。(現在%newamount%)")
                        .replaceAll("&([a-f0-9])", "§$1").replaceAll("%player%", playerName)
                        .replaceAll("%item%", itemName).replaceAll("%amount%", args[3])
                        .replaceAll("%newamount%", String.valueOf(currentAmount + amount)));
            }
            if (subCommand.equalsIgnoreCase("take")) {
                database.set(itemName, playerName, String.valueOf(currentAmount - amount));
                sender.sendMessage(messageConfig
                        .getString("SuccessfullyTake", "&e%player%のボックスの%item%を%amount%減らしました。(現在%newamount%)")
                        .replaceAll("&([a-f0-9])", "§$1").replaceAll("%player%", playerName)
                        .replaceAll("%item%", itemName).replaceAll("%amount%", args[3])
                        .replaceAll("%newamount%", String.valueOf(currentAmount - amount)));
            }
            return true;
        }

        // /boxadmin autostore player item true|false
        if (subCommand.equalsIgnoreCase("autostore")) {

            if (args.length < 2)
                return Commands.errorOccured(sender, noEnoughArguments);

            val player = args[1].toLowerCase();
            if (!database.existPlayer(player))
                return Commands.errorOccured(sender, messageConfig.getString("NoPlayerFound", "&cその名前のプレイヤーは登録されていません。")
                        .replaceAll("&([a-f0-9])", "§$1"));

            List<String> allItems = configManager.getAllItems();

            if (args.length == 2 || (args.length >= 3 && !allItems.contains(args[2].toUpperCase()))) {

                Integer page;
                if (args.length >= 3) {
                    page = Ints.tryParse(args[2]);
                    if (page == null)
                        page = 1;
                } else {
                    page = 1;
                }

                sender.sendMessage(messageConfig
                        .getString("AutoStoreListHeader", "&7=====&6自動回収設定一覧 %page%ページ目 &7(&b%player%&7)=====")
                        .replaceAll("%player%", player).replaceAll("&([a-f0-9])", "§$1")
                        .replaceAll("%page%", page.toString()));
                configManager.getAllItems().stream().map(itemName -> "autostore_" + itemName).skip(10 * (page - 1))
                        .limit(10 * page)
                        .forEach(itemColumnName -> sender
                                .sendMessage(messageConfig.getString("AutoStoreListFormat", "&a%item%&7: &b%isEnabled%")
                                        .replaceAll("%item%", itemColumnName.substring(10))
                                        .replaceAll("%isEnabled%", database.get(itemColumnName, player))));
            }

            val itemName = args[2].toUpperCase();
            String nextValue;
            if (args.length == 3
                    || (args.length > 3 && !Arrays.asList("true", "false").contains(args[3].toLowerCase()))) {
                nextValue = database.get("autostore_" + itemName, player).equalsIgnoreCase("true") ? "false" : "true";
            } else {
                nextValue = args[3].toLowerCase();
            }
            database.set("autostore_" + itemName, player, nextValue);

        }

        return Commands.errorOccured(sender,
                messageConfig.getString("NoParamExist", "&c指定された引数は存在しません。").replaceAll("&([a-f0-9])", "§$1"));

    }
}
