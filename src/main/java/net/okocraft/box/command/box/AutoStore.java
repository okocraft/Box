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

package net.okocraft.box.command.box;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import org.jetbrains.annotations.NotNull;

class AutoStore extends BaseSubCommand {

    private static final String COMMAND_NAME = "autostore";
    private static final int LEAST_ARG_LENGTH = 2;
    private static final String USAGE = "/box autostore < <ITEM> [true|false] | ALL <true|false> >";

    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        // autostore all <true|false>
        if (args.length > 2 && args[1].equalsIgnoreCase("ALL")) {

            // If switchTo is neither true nor false
            if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) {
                sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());

                return false;
            }

            boolean switchTo = args[2].equalsIgnoreCase("true");
            if (PlayerData.setAutoStoreAll((OfflinePlayer) sender, switchTo)) {
                sender.sendMessage(MESSAGE_CONFIG.getAutoStoreSettingChangedAll().replaceAll("%isEnabled%",
                        Boolean.toString(switchTo)));
                
                return true;
            } else {
                sender.sendMessage(MESSAGE_CONFIG.getErrorOccurred());
                return false;
            }
        }

        // autostore Item [true|false]
        String itemName = args[1].toUpperCase();
        ItemStack item = Items.getItemStack(itemName);
        boolean now = PlayerData.getAutoStore((OfflinePlayer) sender, item);
        boolean switchTo = args.length > 2 ? args[2].equalsIgnoreCase("true") : !now;

        if (PlayerData.setAutoStore((OfflinePlayer) sender, item, switchTo)) {
            sender.sendMessage(
                    MESSAGE_CONFIG.getAutoStoreSettingChanged().replaceAll("%item%", itemName)
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

        List<String> ItemStack = new ArrayList<>(CONFIG.getAllItems());
        ItemStack.add("ALL");

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], ItemStack, result);
        }

        String item = args[1].toUpperCase();

        if (!ItemStack.contains(item)) {
            return List.of();
        }

        if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], List.of("true", "false"), result);
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
    protected boolean validate(CommandSender sender, @NotNull String[] args) {
        if (!super.validate(sender, args)) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(MESSAGE_CONFIG.getPlayerOnly());
            return false;
        }

        if (args.length > 2 && (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false"))) {
            sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());
            return false;
        }

        if (args[1].equalsIgnoreCase("ALL") && args.length <= 2) {
            sender.sendMessage(MESSAGE_CONFIG.getNotEnoughArguments());
            return false;
        }

        if (!args[1].equalsIgnoreCase("ALL") && !CONFIG.getAllItems().contains(args[1].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoItemFound());
            return false;
        }

        return true;
    }
}