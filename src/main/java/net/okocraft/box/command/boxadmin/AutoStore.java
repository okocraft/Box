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

package net.okocraft.box.command.boxadmin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.PlayerUtil;

class AutoStore extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "autostore";
    private static final int LEAST_ARG_LENGTH = 3;
    private static final String USAGE = "/boxadmin autostore <player> < <ITEM> [true|false] | ALL <true|false> >";

    @Override
    @SuppressWarnings("deprecation")
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        String playerName = args[1].toLowerCase();
        Map<String, String> players = PlayerData.getPlayers();
        if (!players.containsKey(playerName) && players.containsValue(playerName)) {
            sender.sendMessage(MESSAGE_CONFIG.getNoPlayerFound());
            return false;
        }

        OfflinePlayer player;
        try {
            player = Bukkit.getOfflinePlayer(UUID.fromString(playerName));
        } catch (IllegalArgumentException e) {
            player = Bukkit.getOfflinePlayer(playerName);
        }

        // autostore all <true|false>
        if (args.length > 3 && args[2].equalsIgnoreCase("ALL")) {

            // If switchTo is neither true nor false
            if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) {
                sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());
                
                return false;
            }

            autoStoreAll(sender, player, args[3].equalsIgnoreCase("true"));
            return true;
        }
        
        
        // autostore Item [true|false]
        Items item = Items.valueOf(args[2].toUpperCase());
        boolean now = PlayerData.getAutoStore((OfflinePlayer) sender, item);
        boolean switchTo = args.length > 3 ? args[3].equalsIgnoreCase("true") : !now;
        autoStore(sender, player, item, switchTo);
        return true;
    }

    /**
     * アイテム１つのautoStore設定を変更する。
     * 
     * @param sender
     * @param itemName
     * @param switchTo
     */
    private void autoStore(CommandSender sender, OfflinePlayer player, Items item, boolean switchTo) {
        sender.sendMessage(
                MESSAGE_CONFIG.getAutoStoreSettingChanged()
                        .replaceAll("%item%", item.name()).replaceAll("%isEnabled%", Boolean.toString(switchTo))
        );
        PlayerData.setAutoStore(player, item, switchTo);
    }

    /**
     * アイテムすべてのautoStore設定を変更する。
     * 
     * @param sender
     * @param player
     * @param switchTo
     */
    private void autoStoreAll(CommandSender sender, OfflinePlayer player, boolean switchTo) {
        sender.sendMessage(
                MESSAGE_CONFIG.getAutoStoreSettingChangedAll()
                        .replaceAll("%isEnabled%", Boolean.toString(switchTo))
        );
        PlayerData.setAutoStoreAll(player, switchTo);
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> players = new ArrayList<>(PlayerData.getPlayers().values());

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], players, result);
        }

        if (!players.contains(args[1].toLowerCase())) {
            return List.of();
        }

        List<String> items = new ArrayList<>(CONFIG.getAllItems());
        items.add("ALL");

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], items, result);
        }

        if (!items.contains(args[2].toUpperCase())) {
            return List.of();
        }

        if (args.length == 4) {
            StringUtil.copyPartialMatches(args[3], List.of("true", "false"), result);
        }

        return result;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public int getLeastArgLength() {
        return LEAST_ARG_LENGTH;
    }

    @Override
    public String getUsage() {
        return USAGE;
    }

    @Override
    public String getDescription() {
        return MESSAGE_CONFIG.getAutoStoreDesc();
    }

    @Override
    protected boolean validate(CommandSender sender, String[] args) {
        if (!super.validate(sender, args)) {
            return false;
        }

        if (!PlayerUtil.existPlayer(sender, args[1].toLowerCase())) {
            return false;
        }

        if (!args[2].equalsIgnoreCase("ALL") && !CONFIG.getAllItems().contains(args[2].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoItemFound());
            return false;
        }

        if (args.length < 4 && args[2].equalsIgnoreCase("ALL")) {
            sender.sendMessage(MESSAGE_CONFIG.getNotEnoughArguments());
            return false;
        }

        if (args.length >= 4 && (!args[3].equalsIgnoreCase("true") || !args[3].equalsIgnoreCase("false"))) {
            sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());
            return false;
        }

        return true;
    }
}