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
import java.util.Locale;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

class AutoStoreCommand extends BaseAdminCommand {

    AutoStoreCommand() {
        super(
            "autostore",
            "boxadmin.autostore",
            3,
            false,
            "/boxadmin autostore <player> < <ITEM> [true|false] | ALL <true|false> >",
            new String[0]
        );
    }
    
    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        String playerName = args[1].toLowerCase(Locale.ROOT);

        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (!player.hasPlayedBefore() || player.getName() == null) {
            messages.sendPlayerNotFound(sender);
            return false;
        }

        // autostore all <true|false>
        if (args[2].equalsIgnoreCase("ALL")) {
            if (args.length == 2) {
                messages.sendNotEnoughArguments(sender);
                return false;
            }

            // switchToがtrueでもfalseでもない場合
            if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) {
                messages.sendInvalidArgument(sender, args[3].toLowerCase(Locale.ROOT));
                return false;
            }

            boolean switchTo = args[3].equalsIgnoreCase("true");

            playerData.setAutoStoreAll(player, switchTo);
            messages.sendAutoStoreAll(sender, switchTo);
            return true;
        }

        // autostore Item [true|false]
        String itemName = args[2].toUpperCase(Locale.ROOT);
        if (!categories.getAllItems().contains(itemName)) {
            messages.sendItemNotFound(sender);
            return false;
        }
        ItemStack item = itemData.getItemStack(itemName);
        boolean now = playerData.getAutoStore((OfflinePlayer) sender, item);
        boolean switchTo = args.length > 3 ? args[3].equalsIgnoreCase("true") : !now;

        playerData.setAutoStore(player, item, switchTo);
        messages.sendAutoStore(sender, item, switchTo);
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> players = playerData.getPlayers();

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], players, result);
        }

        if (!players.contains(args[1].toLowerCase(Locale.ROOT))) {
            return List.of();
        }

        Set<String> itemSet = categories.getAllItems();
        itemSet.add("ALL");
        List<String> itemList = new ArrayList<>(itemSet);

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], itemList, result);
        }

        if (!itemSet.contains(args[2].toUpperCase(Locale.ROOT))) {
            return List.of();
        }

        if (args.length == 4) {
            StringUtil.copyPartialMatches(args[3], List.of("true", "false"), result);
        }

        return result;
    }
}