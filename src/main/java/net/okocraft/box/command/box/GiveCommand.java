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
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.command.BaseCommand;
import net.okocraft.box.util.OtherUtil;

class GiveCommand extends BaseCommand {

    GiveCommand() {
        super(
            "give",
            "box.give",
            3,
            true,
            "/box give <player> <ITEM> [amount]",
            new String[0]
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {

        if (sender.getName().equalsIgnoreCase(args[1])) {
            messages.sendCannotGiveMyself(sender);
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
        ItemStack item = itemData.getItemStack(itemName);

        int amount = args.length == 3 ? 1 : OtherUtil.parseIntOrDefault(args[3], 1);
        amount = Math.max(amount, 1);

        int senderStock = playerData.getStock((OfflinePlayer) sender, item);
        int otherStock = playerData.getStock(player, item);

        if (senderStock - amount < 0) {
            messages.sendNotEnoughStock(sender);
            return false;
        }

        playerData.setStock((OfflinePlayer) sender, item, senderStock - amount);
        playerData.setStock(player, item, otherStock + amount);
        messages.sendGiveInfoToSender(sender, player.getName(), itemName, amount, senderStock - amount);
        
        if (player.isOnline()) {
            messages.sendGiveInfoToTarget(player.getPlayer(), sender.getName(), itemName, amount, otherStock + amount);
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

        String player = args[1].toLowerCase(Locale.ROOT);

        if (!players.contains(player)) {
            return List.of();
        }

        List<String> items = playerData.getStockAll((OfflinePlayer) sender).entrySet().parallelStream()
                .filter(entry -> entry.getValue() != 0L).map(Map.Entry::getKey).map(itemData::getName).collect(Collectors.toList());

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], items, result);
        }

        String itemName = args[2].toUpperCase(Locale.ROOT);

        if (!items.contains(itemName)) {
            return List.of();
        }

        int stock = playerData.getStock((OfflinePlayer) sender, itemData.getItemStack(itemName));

        if (stock < 1) {
            return List.of();
        }

        List<String> amountList = IntStream.iterate(1, n -> n * 10).limit(10).filter(n -> n < stock).boxed()
                .map(String::valueOf).collect(Collectors.toList());
        amountList.add(String.valueOf(stock));

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], amountList, result);
        }

        return result;
    }
}