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

import net.okocraft.box.ConfigManager;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

public class BoxAdminCommand implements CommandExecutor {
    private Database database;
    private ConfigManager config;
    private FileConfiguration messageConfig;
    private String notEnoughArguments;

    BoxAdminCommand(Database database) {
        val instance = Box.getInstance();
        this.database = database;

        config        = instance.getConfigManager();
        messageConfig = config.getMessageConfig();
        notEnoughArguments = Optional.ofNullable(messageConfig.getString("NoEnoughArgument"))
                .orElse("&c引数が足りません。")
                .replaceAll("&([a-f0-9])", "§$1");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // only /boxadmin
        if (args.length == 0) {
            return false;
        }

        @NonNull
        val subCommand = args[0];

        // /boxadmin database
        if (subCommand.equalsIgnoreCase("database")) {
            if (args.length == 1) {
                sender.sendMessage(notEnoughArguments);

                return false;
            }

            val databaseSubCommand = args[1].toLowerCase();

            // resetConnection
            if (databaseSubCommand.equalsIgnoreCase("resetconnection")) {
                return resetConnection(sender);
            }

            // addPlayer
            if (databaseSubCommand.equalsIgnoreCase("addplayer")) {
                return addPlayer(sender, args);
            }

            // removePlayer
            if (databaseSubCommand.equalsIgnoreCase("removeplayer")) {
                return removePlayer(sender, args);
            }

            // existPlayer
            if (databaseSubCommand.equalsIgnoreCase("existplayer")) {
                return existPlayer(sender, args);
            }

            // set
            if (databaseSubCommand.equalsIgnoreCase("set")) {
                return databaseSet(sender, args);
            }

            // get
            if (databaseSubCommand.equalsIgnoreCase("get")) {
                return databaseGet(sender, args);
            }

            // addColumn
            if (databaseSubCommand.equalsIgnoreCase("addcolumn")) {
                return addColumn(sender, args);
            }

            // dropColumn
            if (databaseSubCommand.equalsIgnoreCase("dropcolumn")) {
                return dropColumn(sender, args);
            }

            // FIXME: getColumnsMap() と getPlayersMap() の複数形
            // getColumnsMap
            if (databaseSubCommand.equalsIgnoreCase("getcolumnmap")) {
                return getColumnsMap(sender);
            }

            // getPlayersMap
            if (databaseSubCommand.equalsIgnoreCase("getplayersmap")) {
                return getPlayersMap(sender);
            }
        }

        // reload
        if (subCommand.equalsIgnoreCase("reload")) {
            return reload(sender);
        }

        // set|give|take player item [amount]
        if (subCommand.equalsIgnoreCase("set") ||
            subCommand.equalsIgnoreCase("give") ||
            subCommand.equalsIgnoreCase("take") ||
            args.length >= 3) {
            val playerName = args[1];
            val itemName   = args[2].toUpperCase();

            if (!database.existPlayer(playerName)) {
                sender.sendMessage(
                        Optional.ofNullable(messageConfig.getString("NoPlayerFound"))
                                .orElse("&cその名前のプレイヤーは登録されていません。")
                                .replaceAll("&([a-f0-9])", "§$1")
                );

                return false;
            }

            if (!config.getAllItems().contains(itemName)) {
                sender.sendMessage(
                        Optional.ofNullable(messageConfig.getString("NoItemFound"))
                                .orElse("&cその名前のアイテムは登録されていません。")
                                .replaceAll("&([a-f0-9])", "§$1")
                );

                return false;
            }

            // FIXME: Unstable method: Longs#tryParse
            Long amount = Longs.tryParse(args[3]);
            if (amount == null) {
                sender.sendMessage(
                        Optional.ofNullable(messageConfig.getString("InvalidNumberFormat"))
                                .orElse("&c数字のフォーマットが不正です。")
                                .replaceAll("&([a-f0-9])", "§$1")
                );

                return false;
            }

            if (subCommand.equalsIgnoreCase("set")) {
                return set(sender, args[3], playerName, itemName);
            }

            // FIXME: Unstable method: Longs#tryParse
            Long currentAmount = Longs.tryParse(database.get(itemName, playerName));
            if (currentAmount == null) {
                sender.sendMessage(
                        Optional.ofNullable(messageConfig.getString("DatabaseInvalidNumberFormat"))
                                .orElse("&c不正な数字が記録されています。")
                                .replaceAll("&([a-f0-9])", "§$1")
                );

                return false;
            }

            if (subCommand.equalsIgnoreCase("give")) {
                return give(sender, args[3], playerName, itemName, String.valueOf(currentAmount + amount));
            }

            if (subCommand.equalsIgnoreCase("take")) {
                return take(sender, args[3], playerName, itemName, String.valueOf(currentAmount - amount));
            }
        }

        if (subCommand.equalsIgnoreCase("autostore")) {
            return autoStore(sender, args);
        }

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("NoParamExist"))
                        .orElse("&c指定された引数は存在しません。")
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        return false;
    }

    private boolean set(CommandSender sender, String arg, String playerName, String itemName) {
        // FIXME: Unstable method: Longs#tryParse
        Long amount = Longs.tryParse(arg);

        if (amount == null) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("InvalidNumberFormat"))
                            .orElse("&c数字のフォーマットが不正です。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        database.set(itemName, playerName, String.valueOf(amount));

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("SuccessfullySet"))
                        .orElse("&e%player%のボックスの%item%を%amount%にセットしました。")
                        .replaceAll("&([a-f0-9])", "§$1")
                        .replaceAll("%player%", playerName)
                        .replaceAll("%item%", itemName)
                        .replaceAll("%amount%", arg)
        );

        return true;
    }

    private boolean take(CommandSender sender, String arg, String playerName, String itemName, String value) {
        database.set(itemName, playerName, value);

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("SuccessfullyTake"))
                        .orElse("&e%player%のボックスの%item%を%amount%減らしました。(現在%newamount%)")
                        .replaceAll("&([a-f0-9])", "§$1")
                        .replaceAll("%player%", playerName)
                        .replaceAll("%item%", itemName)
                        .replaceAll("%amount%", arg)
                        .replaceAll("%newamount%", value)
        );

        return true;
    }

    private boolean give(CommandSender sender, String arg, String playerName, String itemName, String value) {
        database.set(itemName, playerName, value);

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("SuccessfullyGive"))
                        .orElse("&e%player%のボックスの%item%を%amount%増やしました。(現在%newamount%)")
                        .replaceAll("&([a-f0-9])", "§$1")
                        .replaceAll("%player%", playerName)
                        .replaceAll("%item%", itemName)
                        .replaceAll("%amount%", arg)
                        .replaceAll("%newamount%", value)
        );

        return true;
    }

    /**
     * /boxadmin autostore <player> <item> <true|false>
     *
     * @param sender Sender, player or console
     * @param args   Arguments
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean autoStore(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        val player = args[1].toLowerCase();
        if (!database.existPlayer(player)) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("NoPlayerFound"))
                            .orElse("&cその名前のプレイヤーは登録されていません。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        val allItems = config.getAllItems();

        if (args.length == 2 ||
            !args[2].equalsIgnoreCase("all") &&
            !allItems.contains(args[2].toUpperCase())) {
            // FIXME: Unstable method: Ints#tryParse
            val page = Optional.ofNullable(Ints.tryParse(args[2])).orElse(1);

            int maxLine = allItems.size();
            int currentLine = (maxLine < page * 9) ? maxLine : page * 9;

            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("AutoStoreListHeader"))
                            .orElse("&7=====&6自動回収設定一覧 %page%ページ目 &a%currentline% &7/ &a%maxline% &7(&b%player%&7)=====")
                            .replaceAll("%player%", player)
                            .replaceAll("%currentline%", String.valueOf(currentLine))
                            .replaceAll("%maxline%", String.valueOf(maxLine))
                            .replaceAll("%page%", page.toString())
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            config.getAllItems().stream()
                    .map(itemName -> "autostore_" + itemName)
                    .skip(9 * (page - 1))
                    .limit(9)
                    .forEach(itemColumnName ->
                            sender.sendMessage(
                                    Optional.ofNullable(messageConfig.getString("AutoStoreListFormat"))
                                            .orElse("&a%item%&7: &b%isEnabled%")
                                            .replaceAll("%item%", itemColumnName.substring(10))
                                            .replaceAll("%isEnabled%", database.get(itemColumnName, player))
                                            .replaceAll("%currentline%", String.valueOf(currentLine))
                                            .replaceAll("%maxline%", String.valueOf(maxLine))
                                            .replaceAll("&([a-f0-9])", "§$1")
                            )
                    );

            return true;
        }

        val itemName = args[2].toUpperCase();

        if (itemName.equalsIgnoreCase("all") &&
            args.length == 4 &&
            Arrays.asList("true", "false").contains(args[3].toLowerCase())) {
            val newValues = allItems.stream()
                    .collect(Collectors.toMap(
                            itemNameTemp -> "autostore_" + itemNameTemp,
                            itemNameTemp -> args[3].toLowerCase(),
                            (e1, e2) -> e1, HashMap::new
                    ));

            database.setMultiValue(newValues, player);

            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("AllAutoStoreSettingChanged"))
                            .orElse("&7全てのアイテムのAutoStore設定を&b%isEnabled%&7に設定しました。")
                            .replaceAll("%isEnabled%", args[3].toLowerCase())
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return true;
        }

        if (itemName.equalsIgnoreCase("all") && args.length == 4) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("InvalidArgument"))
                        .orElse("&c引数が不正です。")
                        .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        if (itemName.equalsIgnoreCase("all")) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        if (!allItems.contains(itemName)) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("InvalidArgument"))
                        .orElse("&c引数が不正です。")
                        .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        String nextValue;

        if (args.length == 3 || !Arrays.asList("true", "false").contains(args[3].toLowerCase())) {
            nextValue = database.get("autostore_" + itemName, player)
                    .equalsIgnoreCase("true") ? "false" : "true";
        } else {
            nextValue = args[3].toLowerCase();
        }

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("AutoStoreSettingChanged"))
                        .orElse("&7%item%のAutoStore設定を&b%isEnabled%&7に設定しました。")
                        .replaceAll("%item%", itemName)
                        .replaceAll("%isEnabled%", nextValue)
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        database.set("autostore_" + itemName, player, nextValue);

        return true;
    }

    /**
     * /boxadmin reload
     *
     * @param sender Player or Console
     *
     * @return @code{true} if success, otherwise @{false}
     */
    private boolean reload(CommandSender sender) {
        config.reloadConfig();

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("ConfigReloaded"))
                        .orElse("&7設定を再読込しました。")
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        return true;
    }

    /**
     * /boxadmin database getplayersmap
     *
     * @param sender Player or Console
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean getPlayersMap(CommandSender sender) {
        sender.sendMessage("記録されているプレイヤー");

        database.getPlayersMap().forEach((uuidStr, name) ->
                sender.sendMessage(String.format("%s - %s", uuidStr, name))
        );

        return true;
    }

    /**
     * /boxadmin database getcolumnmap
     *
     * @param sender Player or Console
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean getColumnsMap(CommandSender sender) {
        sender.sendMessage("列リスト");

        database.getColumnMap().forEach((columnName, columnType) ->
                sender.sendMessage(String.format("%s - %s", columnName, columnType))
        );

        return true;
    }

    /**
     * /boxadmin database dropcolumn
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean dropColumn(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        database.dropColumn(args[2]);

        return true;
    }

    /**
     * /boxadmin database addcolumn
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean addColumn(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        if (args[4].equalsIgnoreCase("null")) {
            args[4] = null;
        }

        database.addColumn(args[2], args[3], args[4], true);

        return true;
    }

    /**
     * /boxadmin database get
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean databaseGet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        sender.sendMessage(database.get(args[2], args[3]));

        return true;
    }

    /**
     * /boxadmin database set
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean databaseSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        if (!database.existPlayer(args[3])) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("NoPlayerFound"))
                            .orElse("&cその名前のプレイヤーは登録されていません。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        val uuid = database.get("uuid", args[3]);
        val name = database.get("player", args[3]);

        if (database.getColumnMap().containsKey(args[2])) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("DatabaseNoColumnFound"))
                            .orElse("&cその名前の列はありません。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        database.set(args[2], args[3], args[4]);

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("DatabaseSetValueSuccess"))
                        .orElse("&b%uuid% &7(%player%)の %column% を %value% にセットしました。")
                        .replaceAll("%uuid%", uuid)
                        .replaceAll("%player%", name)
                        .replaceAll("%column%", args[2])
                        .replaceAll("%value%", args[4])
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        return true;
    }

    /**
     * /boxadmin database removeplayer
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean removePlayer(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        if (!database.existPlayer(args[2])) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("NoPlayerFound"))
                            .orElse("&cその名前のプレイヤーは登録されていません。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        val uuid = database.get("uuid", args[2]);
        val name = database.get("player", args[2]);

        database.removePlayer(args[2]);

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("DatabaseRemovePlayerSuccess"))
                        .orElse("&7データベースから&b%uuid% &7- &b%player%&7を削除しました。")
                        .replaceAll("%uuid%", uuid)
                        .replaceAll("%player%", name)
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        return true;
    }

    /**
     * /boxadmin database addplayer
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean addPlayer(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        @SuppressWarnings("deprecation")
        val player = Bukkit.getOfflinePlayer(args[2]);

        if (!player.hasPlayedBefore()) {
            sender.sendMessage(
                    Optional.ofNullable(messageConfig.getString("NoPlayerFound"))
                            .orElse("&cプレイヤーが見つかりませんでした。")
                            .replaceAll("&([a-f0-9])", "§$1")
            );

            return false;
        }

        val uuid = player.getUniqueId().toString();
        val name = Optional.ofNullable(player.getName()).orElse("unknown");

        database.addPlayer(uuid, name, true);

        sender.sendMessage(
                Optional.ofNullable(messageConfig.getString("DatabaseAddPlayerSuccess"))
                        .orElse("&7データベースに&b%uuid% &7- &b%player%&7を追加しました。")
                        .replaceAll("%uuid%", uuid)
                        .replaceAll("%player%", name)
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        return true;
    }

    /**
     * /boxadmin database resetconnection
     *
     * @param sender Player or Console
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean resetConnection(CommandSender sender) {
        database.resetConnection();

        sender.sendMessage("§eデータベースへの接続をリセットしました。");

        return true;
    }

    /**
     * /boxadmin database existplayer
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return @code{true} if success, otherwise @code{false}
     */
    private boolean existPlayer(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage(notEnoughArguments);

            return false;
        }

        sender.sendMessage(String.valueOf(database.existPlayer(args[2])));

        return true;
    }
}
