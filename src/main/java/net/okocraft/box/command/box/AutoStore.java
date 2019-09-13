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
import org.bukkit.util.StringUtil;

import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;

class AutoStore extends BaseSubCommand {

    private static final String COMMAND_NAME = "autostore";
    private static final int LEAST_ARG_LENGTH = 2;
    private static final String USAGE = "/box autostore < <ITEM> [true|false] | ALL <true|false> >";

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
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

            autoStoreAll(sender, args[2].equalsIgnoreCase("true"));
            return true;
        }
        
        
        // autostore Item [true|false]
        Items item = Items.valueOf(args[1].toUpperCase());
        boolean now = PlayerData.getAutoStore((OfflinePlayer) sender, item);
        boolean switchTo = args.length > 2 ? args[2].equalsIgnoreCase("true") : !now;
        autoStore(sender, item, switchTo);
        return true;
    }

    /**
     * アイテム１つのautoStore設定を変更する。
     * 
     * @param sender
     * @param item
     * @param switchTo
     */
    private void autoStore(CommandSender sender, Items item, boolean switchTo) {
        sender.sendMessage(
                MESSAGE_CONFIG.getAutoStoreSettingChanged()
                        .replaceAll("%item%", item.name()).replaceAll("%isEnabled%", Boolean.toString(switchTo))
        );
        PlayerData.setAutoStore((OfflinePlayer) sender, item, switchTo);
    }

    /**
     * アイテムすべてのautoStore設定を変更する。
     * 
     * @param sender
     * @param switchTo
     */
    private void autoStoreAll(CommandSender sender, boolean switchTo) {
        sender.sendMessage(
                MESSAGE_CONFIG.getAutoStoreSettingChangedAll()
                        .replaceAll("%isEnabled%", Boolean.toString(switchTo))
        );
        PlayerData.setAutoStoreAll((OfflinePlayer) sender, switchTo);
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> items = new ArrayList<>(CONFIG.getAllItems());
        items.add("ALL");

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], items, result);
        }

        String item = args[1].toUpperCase();

        if (!items.contains(item)) {
            return List.of();
        }

        if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], List.of("true", "false"), result);
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