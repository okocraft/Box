/*
 * Box
 * Copyright (C) 2019 OKOCRAFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.okocraft.box.command;

import java.util.*;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.val;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.okocraft.box.Box;
import net.okocraft.box.database.Database;
import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.MessageConfig;
import net.okocraft.box.util.OtherUtil;
import net.okocraft.box.util.PlayerUtil;

public class BoxAdminCommand implements CommandExecutor {
    private Database database;
    private GeneralConfig config;
    private MessageConfig messageConfig;

    BoxAdminCommand(Database database) {
        val instance = Box.getInstance();
        this.database = database;

        config        = instance.getGeneralConfig();
        messageConfig = instance.getMessageConfig();
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
                sender.sendMessage(messageConfig.getNotEnoughArguments());

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

            // getColumnsMap
            if (databaseSubCommand.equalsIgnoreCase("getcolumnsmap")) {
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
                sender.sendMessage(messageConfig.getNoPlayerFound());

                return false;
            }

            if (!config.getAllItems().contains(itemName)) {
                sender.sendMessage(messageConfig.getNoItemFound());

                return false;
            }

            Long amount = OtherUtil.parseLongOrDefault(args[3], Long.MIN_VALUE);
            if (amount == Long.MIN_VALUE) {
                sender.sendMessage(messageConfig.getInvalidNumberFormat());

                return false;
            }

            if (subCommand.equalsIgnoreCase("set")) {
                return set(sender, args[3], playerName, itemName);
            }

            Long currentAmount = OtherUtil.parseLongOrDefault(database.get(itemName, playerName), Long.MIN_VALUE);
            if (currentAmount == Long.MIN_VALUE) {
                sender.sendMessage(messageConfig.getInvalidNumberFormat());

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

        sender.sendMessage(messageConfig.getNoParamExist());

        return false;
    }

    private boolean set(CommandSender sender, String arg, String playerName, String itemName) {
        Long amount = OtherUtil.parseLongOrDefault(arg, Long.MIN_VALUE);

        if (amount == Long.MIN_VALUE) {
            sender.sendMessage(messageConfig.getInvalidNumberFormat());

            return false;
        }

        database.set(itemName, playerName, String.valueOf(amount));

        sender.sendMessage(
                messageConfig.getSuccessSet()
                        .replaceAll("%player%", playerName)
                        .replaceAll("%item%", itemName)
                        .replaceAll("%amount%", arg)
        );

        return true;
    }

    private boolean take(CommandSender sender, String arg, String playerName, String itemName, String value) {
        database.set(itemName, playerName, value);

        sender.sendMessage(
                messageConfig.getSuccessTake()
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
                messageConfig.getSuccessGiveAdmin()
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
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean autoStore(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messageConfig.getNotEnoughArguments());

            return false;
        }

        val player = args[1].toLowerCase();
        if (!database.existPlayer(player)) {
            sender.sendMessage(messageConfig.getNoPlayerFound());

            return false;
        }

        val allItems = config.getAllItems();

        if (args.length == 2 ||
            !args[2].equalsIgnoreCase("all") &&
            !allItems.contains(args[2].toUpperCase())) {
            val page = OtherUtil.parseIntOrDefault(args[2], 1);

            val maxLine = allItems.size();
            val currentLine = (maxLine < page * 9) ? maxLine : page * 9;

            sender.sendMessage(
                    messageConfig.getAutoStoreListHeader()
                            .replaceAll("%player%", player)
                            .replaceAll("%currentLine%", String.valueOf(currentLine))
                            .replaceAll("%maxLine%", String.valueOf(maxLine))
                            .replaceAll("%page%", String.valueOf(page))
            );

            config.getAllItems().stream()
                    .map(itemName -> "autostore_" + itemName)
                    .skip(9 * (page - 1))
                    .limit(9)
                    .forEach(itemColumnName ->
                            sender.sendMessage(
                                    messageConfig.getAutoStoreListFormat()
                                            .replaceAll("%item%", itemColumnName.substring(10))
                                            .replaceAll("%isEnabled%", database.get(itemColumnName, player))
                                            .replaceAll("%currentLine%", String.valueOf(currentLine))
                                            .replaceAll("%maxLine%", String.valueOf(maxLine))
                            )
                    );

            return true;
        }

        val itemName = args[2].toUpperCase();

        if (itemName.equalsIgnoreCase("all") &&
            args.length == 4 &&
            List.of("true", "false").contains(args[3].toLowerCase())) {
            val newValues = allItems.stream()
                    .collect(Collectors.toMap(
                            itemNameTemp -> "autostore_" + itemNameTemp,
                            itemNameTemp -> args[3].toLowerCase(),
                            (e1, e2) -> e1, HashMap::new
                    ));

            database.setMultiValue(newValues, player);

            sender.sendMessage(
                    messageConfig.getAutoStoreSettingChangedAll()
                            .replaceAll("%isEnabled%", args[3].toLowerCase())
            );

            return true;
        }

        if (itemName.equalsIgnoreCase("all") && args.length == 4) {
            sender.sendMessage(messageConfig.getInvalidArguments());

            return false;
        }

        if (itemName.equalsIgnoreCase("all")) {
            sender.sendMessage(messageConfig.getNotEnoughArguments());

            return false;
        }

        if (!allItems.contains(itemName)) {
            sender.sendMessage(messageConfig.getInvalidArguments());

            return false;
        }

        String nextValue;

        if (args.length == 3 || !List.of("true", "false").contains(args[3].toLowerCase())) {
            nextValue = database.get("autostore_" + itemName, player)
                    .equalsIgnoreCase("true") ? "false" : "true";
        } else {
            nextValue = args[3].toLowerCase();
        }

        sender.sendMessage(
                messageConfig.getAutoStoreSettingChanged()
                        .replaceAll("%item%", itemName)
                        .replaceAll("%isEnabled%", nextValue)
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
        Box.getInstance().reloadConfig();

        config.reload();
        messageConfig.reload();

        sender.sendMessage(messageConfig.getConfigReloaded());

        return true;
    }

    /**
     * /boxadmin database getplayersmap
     *
     * @param sender Player or Console
     *
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean getPlayersMap(CommandSender sender) {
        sender.sendMessage(messageConfig.getMapPlayersRecord());

        database.getPlayersMap().forEach((uuidStr, name) ->
                sender.sendMessage(String.format(messageConfig.getMapFormat(), uuidStr, name))
        );

        return true;
    }

    /**
     * /boxadmin database getcolumnmap
     *
     * @param sender Player or Console
     *
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean getColumnsMap(CommandSender sender) {
        sender.sendMessage(messageConfig.getMapColumnsList());

        database.getColumnMap().forEach((columnName, columnType) ->
                sender.sendMessage(String.format(messageConfig.getMapFormat(), columnName, columnType))
        );

        return true;
    }

    /**
     * /boxadmin database dropcolumn
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean dropColumn(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage(messageConfig.getNotEnoughArguments());

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
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean addColumn(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(messageConfig.getNotEnoughArguments());

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
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean databaseGet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messageConfig.getNotEnoughArguments());

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
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean databaseSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(messageConfig.getNotEnoughArguments());

            return false;
        }

        if (!database.existPlayer(args[3])) {
            sender.sendMessage(messageConfig.getNoPlayerFound());

            return false;
        }

        val name = database.get("player", args[3]);

        if (database.getColumnMap().containsKey(args[2])) {
            sender.sendMessage(messageConfig.getDatabaseNoColumn());

            return false;
        }

        database.set(args[2], args[3], args[4]);

        sender.sendMessage(
                messageConfig.getSuccessSet()
                        .replaceAll("%player%", name)
                        .replaceAll("%column%", args[2])
                        .replaceAll("%value%", args[4])
        );

        return true;
    }

    /**
     * /boxadmin database removeplayer
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean removePlayer(CommandSender sender, String[] args) {
        if (args.length == 2) {
            // フォーマットされた形で返す
            sender.sendMessage(messageConfig.getNotEnoughArguments());

            return false;
        }

        if (!database.existPlayer(args[2])) {
            sender.sendMessage(messageConfig.getNoPlayerFound());

            return false;
        }

        val uuid = database.get("uuid", args[2]);
        val name = database.get("player", args[2]);

        database.removePlayer(args[2]);

        sender.sendMessage(
                messageConfig.getDatabasePlayerRemoved()
                        .replaceAll("%uuid%", uuid)
                        .replaceAll("%player%", name)
        );

        return true;
    }

    /**
     * /boxadmin database addplayer
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean addPlayer(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage(messageConfig.getNotEnoughArguments());

            return false;
        }

        val player = PlayerUtil.getOfflinePlayer(args[2]);

        if (!player.hasPlayedBefore()) {
            sender.sendMessage(messageConfig.getNoPlayerFound());

            return false;
        }

        val uuid = player.getUniqueId().toString();
        val name = Optional.ofNullable(player.getName()).orElse("unknown");

        database.addPlayer(uuid, name, true);

        sender.sendMessage(
                messageConfig.getDatabasePlayerAdded()
                        .replaceAll("%uuid%", uuid)
                        .replaceAll("%player%", name)
        );

        return true;
    }

    /**
     * /boxadmin database resetconnection
     *
     * @param sender Player or Console
     *
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean resetConnection(CommandSender sender) {
        database.resetConnection();

        sender.sendMessage(messageConfig.getDatabaseConnectionReset());

        return true;
    }

    /**
     * /boxadmin database existplayer
     *
     * @param sender Player or Console
     * @param args   Arguments
     *
     * @return 成功した場合 @code{true}, さもなくば @code{false}
     */
    private boolean existPlayer(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage(messageConfig.getNotEnoughArguments());

            return false;
        }

        sender.sendMessage(String.valueOf(database.existPlayer(args[2])));

        return true;
    }
}
