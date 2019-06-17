/*
 * Box
 * Copyright (C) 2019 AKANE AKAGI
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

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.okocraft.box.Box;
import net.okocraft.box.database.Database;
import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.MessageConfig;

public class BoxCommand implements CommandExecutor {
    private Box instance;
    private Database database;
    private GeneralConfig config;
    private MessageConfig messageConfig;

    BoxCommand(Database database) {
        instance = Box.getInstance();
        this.database = database;

        // config
        config = instance.getGeneralConfig();
        messageConfig = instance.getMessageConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // only /box
        if (args.length == 0) {
            instance.getGuiManager().openCategorySelectionGui((Player) sender);
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

        sender.sendMessage(messageConfig.getNoParamExist());

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

        //
        // NOTE: Don't use Lombok's val.
        //       Otherwise build will be failure due to the compiler interpret `index` as primitive int, not Integer.
        //
        // FIXME: Unstable method: Ints#tryParse
        Integer index = args.length >= 2
                ? Optional.ofNullable(Ints.tryParse(args[1])).orElse(1)
                : 1;

        val allItems = config.getAllItems();
        int maxLine = allItems.size();
        int currentLine = (maxLine < index * 9) ? maxLine : index * 9;

        sender.sendMessage(
                messageConfig.getAutoStoreListHeader()
                        .replaceAll("%player%", sender.getName().toLowerCase())
                        .replaceAll("%page%", index.toString())
                        .replaceAll("%currentline%", String.valueOf(currentLine))
                        .replaceAll("%maxline%", String.valueOf(maxLine))
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        val columnList = config.getAllItems().stream()
                .skip(9 * (index - 1))
                .limit(9)
                .map(name -> "autostore_" + name)
                .collect(Collectors.toList());

        database.getMultiValue(columnList, player).forEach((columnName, value) -> {
            sender.sendMessage(
                    messageConfig.getAutoStoreListFormat()
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
            sender.sendMessage(messageConfig.getNotEnoughArguments());

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
        val allItems = config.getAllItems();

        // If switchTo is neither true nor false
        if (!switchTo.equalsIgnoreCase("true") && !switchTo.equalsIgnoreCase("false")) {
            sender.sendMessage(messageConfig.getInvalidArguments());

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
                messageConfig.getAutoStoreSettingChangedAll()
                        .replaceAll("%isEnabled%", switchTo.toLowerCase())
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
        val allItems = config.getAllItems();

        if (!allItems.contains(itemName)) {
            sender.sendMessage(messageConfig.getInvalidArguments());

            return false;
        }

        String switchTo;

        if (args.length >= 3) {
            if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) {
                sender.sendMessage(messageConfig.getInvalidArguments());

                return false;
            }

            switchTo = args[2].toLowerCase();
        } else {
            val now = database.get("autostore_" + itemName, player).equalsIgnoreCase("true");
            switchTo = now ? "false" : "true";
        }

        sender.sendMessage(
                messageConfig.getAutoStoreSettingChanged()
                        .replaceAll("%item%", itemName).replaceAll("%isEnabled%", switchTo)
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
            sender.sendMessage(messageConfig.getNotEnoughArguments());

            return false;
        }

        val senderName = sender.getName().toLowerCase();
        val player = args[1].toLowerCase();

        if (senderName.equals(player)) {
            sender.sendMessage(messageConfig.getCannotGiveYourself());

            return false;
        }

        val itemName = args[2].toUpperCase();
        if (!config.getAllItems().contains(itemName)) {
            sender.sendMessage(messageConfig.getNoItemFound());

            return false;
        }

        if (!database.existPlayer(player)) {
            sender.sendMessage(messageConfig.getNoPlayerFound());

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
                    messageConfig.getDatabaseInvalidValue()
            );

            return false;
        }

        if (senderAmount - amount < 0) {
            sender.sendMessage(
                    messageConfig.getNotEnoughStoredItem()
            );

            return false;
        }

        database.set(itemName, senderName, String.valueOf(senderAmount - amount));
        database.set(itemName, player, String.valueOf(otherAmount + amount));

        sender.sendMessage(
                messageConfig.getSuccessGive()
                        .replaceAll("%player%", player).replaceAll("%item%", itemName)
                        .replaceAll("%amount%", amount.toString())
                        .replaceAll("%newamount%", String.valueOf(senderAmount - amount))
                        .replaceAll("&([a-f0-9])", "§$1")
        );

        val offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(database.get("uuid", player)));

        if (offlinePlayer.isOnline()) {
            Optional.ofNullable(offlinePlayer.getPlayer()).ifPresent( _player ->
                    _player.sendMessage(
                            messageConfig.getSuccessGive()
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
        sender.sendMessage(
                messageConfig.getVersionInfo()
                    .replaceAll("%version%", Box.getInstance().getVersion())
        );

        return true;
    }

    private boolean notExistPlayer(CommandSender sender) {
        val player = ((Player) sender).getUniqueId().toString();

        if (!database.existPlayer(player)) {
            sender.sendMessage(messageConfig.getNoPlayerFound());

            return true;
        }

        return false;
    }
}
