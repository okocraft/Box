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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.OtherUtil;

class TakeCommand extends BaseAdminCommand {

    TakeCommand() {
        super(
            "take",
            "boxadmin.take",
            3,
            false,
            "/boxadmin take <player> <ITEM> [amount]",
            new String[0]
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!PlayerData.exist(args[1])) {
            messages.sendPlayerNotFound(sender);
            return false;
        }

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
        ItemStack item = Items.getItemStack(itemName);
        long amount = args.length < 4 ? 1 : OtherUtil.parseLongOrDefault(args[3], 1);
        long stock = PlayerData.getItemAmount(player, item);

        PlayerData.setItemAmount(player, item, stock - amount);

        messages.sendTakeInfoToSender(sender, player.getName(), itemName, amount, stock - amount);
        if (player.isOnline()) {
            messages.sendTakeInfoToTarget(sender, sender.getName(), itemName, amount, stock - amount);
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

        String playerName = args[1].toLowerCase(Locale.ROOT);

        if (!players.contains(playerName)) {
            return List.of();
        }

        List<String> items = new ArrayList<>(Items.getItems());

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], items, result);
        }

        String item = args[2].toUpperCase(Locale.ROOT);

        if (!items.contains(item)) {
            return List.of();
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        long stock = PlayerData.getItemAmount(offlinePlayer, Items.getItemStack(item));

        List<String> amountList = IntStream.iterate(1, n -> n * 10).limit(10).filter(n -> n < stock)
                .boxed().map(String::valueOf).collect(Collectors.toList());
        amountList.add(String.valueOf(stock));

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], amountList, result);
        }

        return result;
    }
}