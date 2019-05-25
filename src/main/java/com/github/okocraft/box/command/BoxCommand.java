package com.github.okocraft.box.command;

import java.util.Arrays;
import java.util.List;

import com.github.okocraft.box.ConfigManager;
import com.github.okocraft.box.Box;
import com.github.okocraft.box.database.Database;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import lombok.NonNull;
import lombok.val;

public class BoxCommand implements CommandExecutor {

    private Database database;
    private Box instance;
    private ConfigManager configManager;
    private FileConfiguration messageConfig;
    private String noEnoughArguments;

    protected BoxCommand(Database database) {
        this.database = database;
        instance = Box.getInstance();
        configManager = instance.getConfigManager();
        messageConfig = configManager.getMessageConfig();
        noEnoughArguments = messageConfig.getString("NoEnoughArgument", "&c引数が足りません。").replaceAll("&([a-f0-9])", "§$1");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // only /box
        if (args.length == 0) {
            instance.getGuiManager().openCategorySelectionGui((Player) sender, 1, true);
            return true;
        }

        @NonNull
        val subCommand = args[0];

        // Box version
        if (subCommand.equalsIgnoreCase("version")) {
            sender.sendMessage(Box.getInstance().getVersion());

            return true;
        }

        // /boxadmin autostore item true|false
        if (subCommand.equalsIgnoreCase("autostore")) {

            if (args.length < 1)
                return Commands.errorOccured(sender, noEnoughArguments);

            val player = ((Player) sender).getUniqueId().toString();
            if (!database.existPlayer(player))
                return Commands.errorOccured(sender, messageConfig.getString("NoPlayerFound", "&cその名前のプレイヤーは登録されていません。")
                        .replaceAll("&([a-f0-9])", "§$1"));

            List<String> allItems = configManager.getAllItems();

            if (args.length == 1 || (args.length >= 2 && !allItems.contains(args[1].toUpperCase()))) {

                Integer page;
                if (args.length >= 2) {
                    page = Ints.tryParse(args[1]);
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
                return true;
            }

            val itemName = args[1].toUpperCase();
            String nextValue;
            if (args.length == 2
                    || (args.length > 1 && !Arrays.asList("true", "false").contains(args[2].toLowerCase()))) {
                nextValue = database.get("autostore_" + itemName, player).equalsIgnoreCase("true") ? "false" : "true";
            } else {
                nextValue = args[2].toLowerCase();
            }
            database.set("autostore_" + itemName, player, nextValue);

        }

        // for /box give player item [amount]
        if (subCommand.equalsIgnoreCase("give")) {
            if (args.length < 3) {
                return Commands.errorOccured(sender, noEnoughArguments);
            }
            val senderName = ((Player) sender).getName();
            val player = args[1];
            val itemName = args[2].toUpperCase();

            if (!database.existPlayer(player))
                return Commands.errorOccured(sender, messageConfig.getString("NoPlayerFound", "&cその名前のプレイヤーは登録されていません。")
                        .replaceAll("&([a-f0-9])", "§$1"));

            if (!configManager.getAllItems().contains(itemName)) {
                return Commands.errorOccured(sender, messageConfig.getString("NoItemFound", "&cその名前のアイテムは登録されていません。")
                        .replaceAll("&([a-f0-9])", "§$1"));
            }

            Long amount;
            if (args.length == 3)
                amount = 1L;
            else
                amount = Longs.tryParse(args[3]);

            if (amount == null) {
                return Commands.errorOccured(sender, messageConfig.getString("InvalidNumberFormat", "&c数字のフォーマットが不正です。")
                        .replaceAll("&([a-f0-9])", "§$1"));
            }

            Long currentSenderAmount = Longs.tryParse(database.get(itemName, senderName));
            Long currentOtherAmount = Longs.tryParse(database.get(itemName, player));
            if (currentSenderAmount == null || currentOtherAmount == null) {
                return Commands.errorOccured(sender,
                        messageConfig.getString("DatabaseInvalidNumberFormat", "&c不正な数字が記録されています。管理者に報告して下さい。")
                                .replaceAll("&([a-f0-9])", "§$1"));
            }
            if (currentSenderAmount - amount < 0) {
                return Commands.errorOccured(sender, messageConfig.getString("NoEnoughStore", "&c在庫が足りません。").replaceAll("&([a-f0-9])", "§$1"));
            }
            database.set(itemName, player, String.valueOf(currentSenderAmount - amount));
            database.set(itemName, player, String.valueOf(currentOtherAmount + amount));
            sender.sendMessage(messageConfig
                    .getString("SuccessfullyGiveNonAdmin", "&e%player%に%item%を%amount%個渡しました。(現在%newamount%)")
                    .replaceAll("&([a-f0-9])", "§$1").replaceAll("%player%", player).replaceAll("%item%", itemName)
                    .replaceAll("%amount%", amount.toString())
                    .replaceAll("%newamount%", String.valueOf(currentSenderAmount - amount)));
            return true;
        }

        return false;
    }

}