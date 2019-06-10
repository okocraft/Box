package net.okocraft.box.command;

import java.util.*;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.val;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.okocraft.box.Box;
import net.okocraft.box.ConfigManager;
import net.okocraft.box.database.Database;

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
            instance.getGuiManager().openCategorySelectionGui((Player) sender, true);
            return true;
        }

        @NonNull
        val subCommand = args[0];

        // Box version
        if (subCommand.equalsIgnoreCase("version")) {
            sender.sendMessage(Box.getInstance().getVersion());

            return true;
        }

        // /box autostore item true|false
        if (subCommand.equalsIgnoreCase("autostore")) {

            if (args.length < 1)
                return Commands.errorOccured(sender, noEnoughArguments);

            val player = ((Player) sender).getUniqueId().toString();
            if (!database.existPlayer(player))
                return Commands.errorOccured(sender, messageConfig.getString("NoPlayerFound", "&cその名前のプレイヤーは登録されていません。")
                        .replaceAll("&([a-f0-9])", "§$1"));

            List<String> allItems = configManager.getAllItems();

            if (args.length == 1 || (args.length == 2 && !args[1].equalsIgnoreCase("all")
                    && !allItems.contains(args[1].toUpperCase()))) {

                Integer page;
                if (args.length >= 2) {
                    page = Ints.tryParse(args[1]);
                    if (page == null)
                        page = 1;
                } else {
                    page = 1;
                }

                int maxLine = allItems.size();
                int currentLine = (maxLine < page * 9) ? maxLine : page * 9;

                sender.sendMessage(messageConfig
                        .getString("AutoStoreListHeader",
                                "&7=====&6自動回収設定一覧 %page%ページ目 &a%currentline% &7/ &a%maxline% &7(&b%player%&7)=====")
                        .replaceAll("%player%", sender.getName().toLowerCase()).replaceAll("%page%", page.toString())
                        .replaceAll("%currentline%", String.valueOf(currentLine))
                        .replaceAll("%maxline%", String.valueOf(maxLine)).replaceAll("&([a-f0-9])", "§$1"));

                List<String> columnList = configManager.getAllItems().stream().skip(9 * (page - 1)).limit(9)
                        .map(itemName -> "autostore_" + itemName).collect(Collectors.toList());

                database.getMultiValue(columnList, player).forEach((columnName, value) -> {
                    sender.sendMessage(messageConfig.getString("AutoStoreListFormat", "&a%item%&7: &b%isEnabled%")
                            .replaceAll("%item%", columnName.substring(10)).replaceAll("%isEnabled%", value)
                            .replaceAll("%currentline%", String.valueOf(currentLine))
                            .replaceAll("%maxline%", String.valueOf(maxLine)).replaceAll("&([a-f0-9])", "§$1"));
                });
                return true;
            }

            val itemName = args[1].toUpperCase();

            if (itemName.equalsIgnoreCase("all") && args.length == 3
                    && Arrays.asList("true", "false").contains(args[2].toLowerCase())) {
                Map<String, String> newValues = allItems.stream()
                        .collect(Collectors.toMap(itemNameTemp -> "autostore_" + itemNameTemp,
                                itemNameTemp -> args[2].toLowerCase(), (e1, e2) -> e1, HashMap::new));

                database.setMultiValue(newValues, player);
                sender.sendMessage(messageConfig
                        .getString("AllAutoStoreSettingChanged", "&7全てのアイテムのAutoStore設定を&b%isEnabled%&7に設定しました。")
                        .replaceAll("%isEnabled%", args[2].toLowerCase()).replaceAll("&([a-f0-9])", "§$1"));
                return true;
            } else if (itemName.equalsIgnoreCase("all") && args.length == 3) {
                return Commands.errorOccured(sender,
                        messageConfig.getString("InvalidArgument", "&c引数が不正です。").replaceAll("&([a-f0-9])", "§$1"));
            } else if (itemName.equalsIgnoreCase("all")) {
                return Commands.errorOccured(sender, noEnoughArguments);
            }

            if (!allItems.contains(itemName)) {
                return Commands.errorOccured(sender,
                        messageConfig.getString("InvalidArgument", "&c引数が不正です。").replaceAll("&([a-f0-9])", "§$1"));
            }

            String nextValue;
            if (args.length == 2
                    || (args.length > 1 && !Arrays.asList("true", "false").contains(args[2].toLowerCase()))) {
                nextValue = database.get("autostore_" + itemName, player).equalsIgnoreCase("true") ? "false" : "true";
            } else {
                nextValue = args[2].toLowerCase();
            }
            sender.sendMessage(
                    messageConfig.getString("AutoStoreSettingChanged", "&7%item%のAutoStore設定を&b%isEnabled%&7に設定しました。")
                            .replaceAll("%item%", itemName).replaceAll("%isEnabled%", nextValue)
                            .replaceAll("&([a-f0-9])", "§$1"));
            database.set("autostore_" + itemName, player, nextValue);
            return true;
        }

        // for /box give player item [amount]
        if (subCommand.equalsIgnoreCase("give")) {
            if (args.length < 3) {
                return Commands.errorOccured(sender, noEnoughArguments);
            }
            val senderName = ((Player) sender).getName().toLowerCase();
            val player = args[1].toLowerCase();
            val itemName = args[2].toUpperCase();

            if (senderName.equals(player))
                return Commands.errorOccured(sender, messageConfig
                        .getString("CannotGiveYourself", "&c自分自身にアイテムを渡すことはできません。").replaceAll("&([a-f0-9])", "§$1"));

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
                return Commands.errorOccured(sender,
                        messageConfig.getString("NoEnoughStore", "&c在庫が足りません。").replaceAll("&([a-f0-9])", "§$1"));
            }
            database.set(itemName, senderName, String.valueOf(currentSenderAmount - amount));
            database.set(itemName, player, String.valueOf(currentOtherAmount + amount));
            sender.sendMessage(messageConfig
                    .getString("SuccessfullyGiveNonAdmin", "&e%player%に%item%を%amount%個渡しました。(現在%newamount%)")
                    .replaceAll("%player%", player).replaceAll("%item%", itemName)
                    .replaceAll("%amount%", amount.toString())
                    .replaceAll("%newamount%", String.valueOf(currentSenderAmount - amount))
                    .replaceAll("&([a-f0-9])", "§$1"));

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(database.get("uuid", player)));
            if (offlinePlayer.isOnline())
                offlinePlayer.getPlayer()
                        .sendMessage(messageConfig
                                .getString("GivenItemFromNonAdmin",
                                        "&b%player%&7から&b%item%&7を&b%amount%&7個貰いました。(現在%newamount%個)")
                                .replaceAll("%player%", senderName).replaceAll("%item%", itemName)
                                .replaceAll("%amount%", amount.toString()).replaceAll("%newamount%",
                                        String.valueOf(currentOtherAmount + amount).replaceAll("&([a-f0-9])", "§$1")));

            return true;
        }

        return false;
    }

}
