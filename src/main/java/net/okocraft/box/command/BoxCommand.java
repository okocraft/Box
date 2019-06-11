package net.okocraft.box.command;

import java.util.*;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.val;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import org.bukkit.Bukkit;
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
    private String notEnoughArguments;

    BoxCommand(Database database) {
        this.database = database;
        instance = Box.getInstance();
        configManager = instance.getConfigManager();
        messageConfig = configManager.getMessageConfig();
        notEnoughArguments = Optional.ofNullable(messageConfig.getString("NoEnoughArgument"))
                .orElse("&c引数が足りません。")
                .replaceAll("&([a-f0-9])", "§$1");
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
            return version(sender);
        }

        // /box autoStoreList
        if (subCommand.equalsIgnoreCase("autostorelist")) {
            return autoStoreList(sender, args);
        }

        // /box autostore <item [true|false] | all <true|false>>
        if (subCommand.equalsIgnoreCase("autostore")) {
            return autoStore(sender, args);
        }

        // /box give <player> <item> [amount]
        if (subCommand.equalsIgnoreCase("give")) {
            return give(sender, args);
        }

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("NoParamExist"))
                        .orElse("&c指定された引数は存在しません。")
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        return false;
    }

    /**
     * /box autostorelist: 自動収集の設定一覧を表示する
     *
     * @param sender Sender
     * @param args   Arguments
     *
     * @return true if success, otherwise false
     */
    private boolean autoStoreList(CommandSender sender, String[] args) {
        // If the sender is not player(= is console)
        if (!(sender instanceof Player)) {
            return false;
        }

        // If the player isn't registered
        if (notExistPlayer(sender)) {
            return false;
        }

        val player = ((Player) sender).getUniqueId().toString();

        // FIXME: Unstable method: Ints#tryParse
        // Set index
        val index = args.length >= 2
                ? Optional.ofNullable(Ints.tryParse(args[1])).orElse(1)
                : 1;

        val allItems = configManager.getAllItems();
        int maxLine = allItems.size();
        int currentLine = (maxLine < index * 9) ? maxLine : index * 9;

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("AutoStoreListHeader"))
                        .orElse("&7=====&6自動回収設定一覧 %page%ページ目 &a%currentline% &7/ &a%maxline% &7(&b%player%&7)=====")
                        .replaceAll("%player%", sender.getName().toLowerCase())
                        .replaceAll("%page%", index.toString())
                        .replaceAll("%currentline%", String.valueOf(currentLine))
                        .replaceAll("%maxline%", String.valueOf(maxLine))
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        val columnList = configManager.getAllItems().stream()
                .skip(9 * (index - 1))
                .limit(9)
                .map(name -> "autostore_" + name)
                .collect(Collectors.toList());

        database.getMultiValue(columnList, player).forEach((columnName, value) -> {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("AutoStoreListFormat"))
                            .orElse("&a%item%&7: &b%isEnabled%")
                            .replaceAll("%item%", columnName.substring(10))
                            .replaceAll("%isEnabled%", value)
                            .replaceAll("%currentline%", String.valueOf(currentLine))
                            .replaceAll("%maxline%", String.valueOf(maxLine))
                            .replaceAll("&([a-f0-9])", "§$1")
            );
        });

        return true;
    }

    /**
     * /box autostore <item [true|false] | all <true|false>>
     *
     * @param sender Sender
     * @param args   Arguments
     *
     * @return true if success, otherwise false
     */
    private boolean autoStore(CommandSender sender, String[] args) {
        // len   0    1         2    3
        // args [X]  [0]       [1]  [2]
        // cmd  /box autostore item true|false

        if (args.length < 2) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        if (notExistPlayer(sender)) {
            return false;
        }

        val itemName = args[1].toUpperCase();

        // autostore all <true|false>
        if (itemName.equalsIgnoreCase("all") && args.length == 3) {
            return autoStoreAll(sender, args[2]);
        }

        // autostore Item [true|false]
        return autoStoreItem(sender, args);

    }

    /**
     * /box autostore all <true|false>
     *
     * @param sender   Sender
     * @param switchTo Switch
     *
     * @return true if success, otherwise false
     *
     */
    private boolean autoStoreAll(CommandSender sender, String switchTo) {
        val player   = ((Player) sender).getUniqueId().toString();
        val allItems = configManager.getAllItems();

        // If switchTo is neither true nor false
        if (!switchTo.equalsIgnoreCase("true") && !switchTo.equalsIgnoreCase("false")) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("InvalidArgument"))
                            .orElse("&c引数が不正です。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        val newValues = allItems.stream()
                .collect(Collectors.toMap(itemNameTemp ->
                                "autostore_" + itemNameTemp,
                        itemNameTemp -> switchTo.toLowerCase(),
                        (e1, e2) -> e1, HashMap::new
                ));

        database.setMultiValue(newValues, player);

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("AllAutoStoreSettingChanged"))
                        .orElse("&7すべてのアイテムの AutoStore 設定を &b%isEnabled%&7 に設定しました。")
                        .replaceAll("%isEnabled%", switchTo.toLowerCase())
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        return true;
    }

    /**
     * /box autostore <item> [true|false]
     *
     * @param sender Sender
     * @param args   Arguments
     *
     * @return true if success, otherwise false
     */
    private boolean autoStoreItem(CommandSender sender, String[] args) {
        val player   = ((Player) sender).getUniqueId().toString();
        val itemName = args[1].toUpperCase();
        val allItems = configManager.getAllItems();

        if (!allItems.contains(itemName)) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("InvalidArgument"))
                            .orElse("&c引数が不正です。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        String switchTo;

        if (args.length >= 3) {
            if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) {
                sender.sendMessage(
                        Optional.ofNullable(messageConfig.getString("InvalidArgument"))
                                .orElse("&c引数が不正です。")
                                .replaceAll("&([a-f0-9])", "§$1")
                );

                return false;
            }

            switchTo = args[2].toLowerCase();
        } else {
            val now = database.get("autostore_" + itemName, player).equalsIgnoreCase("true");
            switchTo = now ? "false" : "true";
        }

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("AutoStoreSettingChanged"))
                        .orElse("&7%item%のAutoStore設定を&b%isEnabled%&7に設定しました。")
                        .replaceAll("%item%", itemName).replaceAll("%isEnabled%", switchTo)
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        database.set("autostore_" + itemName, player, switchTo);

        return true;
    }

    /**
     * /box give <player> <item> <amount>
     *
     * @param sender Sender
     * @param args   Arguments
     *
     * @return true if success, otherwise false
     */
    private boolean give(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        val senderName = sender.getName().toLowerCase();
        val player = args[1].toLowerCase();

        if (senderName.equals(player)) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("CannotGiveYourself"))
                            .orElse("&c自分自身にアイテムを渡すことはできません。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        val itemName = args[2].toUpperCase();
        if (!configManager.getAllItems().contains(itemName)) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("NoItemFound"))
                            .orElse("&cその名前のアイテムは登録されていません。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        if (!database.existPlayer(player)) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("NoPlayerFound"))
                            .orElse("&cその名前のプレイヤーは登録されていません。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        Long amount = args.length == 3
                // FIXME: Unstable method: Longs#tryParse
                ? Optional.ofNullable(Longs.tryParse(args[3])).orElse(1L)
                : 1L;

        // FIXME: Unstable method: Longs#tryParse
        val senderAmount = Longs.tryParse(database.get(itemName, senderName));
        // FIXME: Unstable method: Longs#tryParse
        val otherAmount  = Longs.tryParse(database.get(itemName, player));

        if (senderAmount == null || otherAmount == null) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("DatabaseInvalidNumberFormat"))
                            .orElse("&c不正な数字が記録されています。管理者に報告して下さい。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        if (senderAmount - amount < 0) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("NoEnoughStore"))
                            .orElse("&c在庫が足りません。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        database.set(itemName, senderName, String.valueOf(senderAmount - amount));
        database.set(itemName, player, String.valueOf(otherAmount + amount));

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("SuccessfullyGiveNonAdmin"))
                        .orElse("&e%player%に%item%を%amount%個渡しました。(現在%newamount%)")
                        .replaceAll("%player%", player).replaceAll("%item%", itemName)
                        .replaceAll("%amount%", amount.toString())
                        .replaceAll("%newamount%", String.valueOf(senderAmount - amount))
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(database.get("uuid", player)));

        if (offlinePlayer.isOnline()) {
            Optional.ofNullable(offlinePlayer.getPlayer()).ifPresent( _player ->
                    _player.sendMessage(
                            Optional.ofNullable(messageConfig.getString("GivenItemFromNonAdmin"))
                                    .orElse("&b%player%&7から&b%item%&7を&b%amount%&7個貰いました。(現在%newamount%個)")
                                    .replaceAll("%player%", senderName)
                                    .replaceAll("%item%", itemName)
                                    .replaceAll("%amount%", amount.toString())
                                    .replaceAll(
                                            "%newamount%",
                                            String.valueOf(otherAmount + amount)
                                                    .replaceAll("&([a-f0-9])", "§$1")
                                    )
                    )
            );
        }

        return true;
    }

    private boolean version(CommandSender sender) {
        sender.sendMessage(Box.getInstance().getVersion());

        return true;
    }

    private boolean notExistPlayer(CommandSender sender) {
        val player = ((Player) sender).getUniqueId().toString();

        if (!database.existPlayer(player)) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("NoPlayerFound"))
                            .orElse("&cその名前のプレイヤーは登録されていません。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return true;
        }

        return false;
    }
}
