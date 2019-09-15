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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.PlayerUtil;
import org.jetbrains.annotations.NotNull;

class AutoStore extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "autostore";
    private static final int LEAST_ARG_LENGTH = 3;
    private static final String USAGE = "/boxadmin autostore <player> < <ITEM> [true|false] | ALL <true|false> >";

    @Override
    @SuppressWarnings("deprecation")
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
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

            boolean switchTo = args[3].equalsIgnoreCase("true");

            if (PlayerData.setAutoStoreAll(player, switchTo)) {
                sender.sendMessage(MESSAGE_CONFIG.getAutoStoreSettingChangedAll().replaceAll("%isEnabled%",
                        Boolean.toString(switchTo)));
                return true;
            } else {
                sender.sendMessage(MESSAGE_CONFIG.getErrorOccurred());
                return false;
            }
        }

        // autostore Item [true|false]
        String itemName = args[2].toUpperCase();
        ItemStack item = Items.getItemStack(itemName);
        boolean now = PlayerData.getAutoStore((OfflinePlayer) sender, item);
        boolean switchTo = args.length > 3 ? args[3].equalsIgnoreCase("true") : !now;

        if (PlayerData.setAutoStore(player, item, switchTo)) {
            sender.sendMessage(MESSAGE_CONFIG.getAutoStoreSettingChanged().replaceAll("%item%", itemName)
                    .replaceAll("%isEnabled%", Boolean.toString(switchTo)));
            return true;
        } else {
            sender.sendMessage(MESSAGE_CONFIG.getErrorOccurred());
            return false;
        }
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, @NotNull String[] args) {
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

    @NotNull
    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public int getLeastArgLength() {
        return LEAST_ARG_LENGTH;
    }

    @NotNull
    @Override
    public String getUsage() {
        return USAGE;
    }

    @Override
    public String getDescription() {
        return MESSAGE_CONFIG.getAutoStoreDesc();
    }

    @Override
    protected boolean validate(@NotNull CommandSender sender, @NotNull String[] args) {
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

        if (args.length >= 4 && !args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) {
            sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());
            return false;
        }

        return true;
    }
}