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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;


class SetCommand extends BaseAdminCommand {

    SetCommand() {
        super(
            "set",
            "boxadmin.set",
            3,
            false,
            "/boxadmin set <player> <ITEM> <amount>",
            new String[0]
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        if (!player.hasPlayedBefore() || player.getName() == null) {
            messages.sendPlayerNotFound(sender);
            return false;
        }

        String itemName = args[2].toUpperCase(Locale.ROOT);
        if (!categories.getAllItems().contains(itemName)) {
            messages.sendItemNotFound(sender);
            return false;
        }
        ItemStack item = itemData.getItemStack(itemName);
        int amount = args.length < 4 ? 1 : parseIntOrDefault(args[3], 1);
        playerData.setStock(player, item, amount);

        messages.sendSetInfoToSender(sender, player.getName(), item, amount);
        if (player.isOnline()) {
            messages.sendSetInfoToTarget(sender, sender.getName(), item, amount);
        }

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

        List<String> items = new ArrayList<>(categories.getAllItems());

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