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
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.config.Categories;
import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.OtherUtil;
import net.okocraft.box.util.PlayerUtil;

class Set extends BoxAdminSubCommand {
    
    Set() {
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!PlayerData.exist(args[1])) {
            MESSAGES.sendMessage(sender, "command.general.error.player-not-found");
            return false;
        }

        OfflinePlayer player = PlayerUtil.getOfflinePlayer(args[1]);
        if (!player.hasPlayedBefore() || player.getName() == null) {
            MESSAGES.sendMessage(sender, "command.general.error.player-not-found");
            return false;
        }

        String itemName = args[2].toUpperCase(Locale.ROOT);
        if (!Categories.getInstance().getAllItems().contains(itemName)) {
            MESSAGES.sendMessage(sender, "command.general.error.item-not-found");
            return false;
        }
        ItemStack item = Items.getItemStack(itemName);
        long amount = args.length < 4 ? 1 : OtherUtil.parseLongOrDefault(args[3], 1);
        PlayerData.setItemAmount(player, item, amount);

        MESSAGES.sendMessage(sender, "command.box-admin.set.info.sender", Map.of(
                "%player%", player.getName(),
                "%item%", itemName,
                "%amount%", String.valueOf(amount)
        ));
        
        if (player.isOnline()) {
            MESSAGES.sendMessage(player.getPlayer(), "command.box-admin.set.info.player", Map.of(
                    "%sender%", sender.getName(),
                    "%item%", itemName,
                    "%amount%", String.valueOf(amount)
            ));
        }

        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> players = new ArrayList<>(PlayerData.getPlayers().values());

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], players, result);
        }

        if (!players.contains(args[1].toLowerCase(Locale.ROOT))) {
            return List.of();
        }

        List<String> items = new ArrayList<>(Categories.getInstance().getAllItems());

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], items, result);
        }

        if (!items.contains(args[2].toUpperCase(Locale.ROOT))) {
            return List.of();
        }

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], List.of("1", "10", "100", "1000", "10000"), result);
        }

        return result;
    }


    @Override
    public int getLeastArgLength() {
        return 3;
    }

    @Override
    public String getUsage() {
        return "/boxadmin set <player> <ITEM> <amount>";
    }
}