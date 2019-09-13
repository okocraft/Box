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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.OtherUtil;

class Give extends BaseSubCommand {

    private static final String COMMAND_NAME = "give";
    private static final int LEAST_ARG_LENGTH = 3;
    private static final String USAGE = "/box give <player> <ITEM> [amount]";

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        if (!player.hasPlayedBefore()) {
            sender.sendMessage(MESSAGE_CONFIG.getNoPlayerFound());
            return false;
        }
        
        Items item = Items.valueOf(args[2].toUpperCase());

        long amount = args.length == 3 ? 1L : OtherUtil.parseLongOrDefault(args[3], 1L);

        if (amount < 1) {
            sender.sendMessage(MESSAGE_CONFIG.getInvalidArguments());
            return false;
        }

        long senderAmount = PlayerData.getItemAmount((OfflinePlayer) sender, item);
        long otherAmount = PlayerData.getItemAmount(player, item);

        if (senderAmount - amount < 0) {
            sender.sendMessage(
                    MESSAGE_CONFIG.getNotEnoughStoredItem()
            );

            return false;
        }

        PlayerData.setItemAmount((OfflinePlayer) sender, item, senderAmount - amount);
        PlayerData.setItemAmount(player, item, otherAmount + amount);

        sender.sendMessage(
                MESSAGE_CONFIG.getSuccessGive()
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%item%", item.name())
                        .replaceAll("%amount%", Long.toString(amount))
                        .replaceAll("%newamount%", Long.toString(senderAmount - amount))
        );

        if (player.isOnline()) {
            player.getPlayer().sendMessage(
                    MESSAGE_CONFIG.getSuccessReceive()
                            .replaceAll("%player%", sender.getName())
                            .replaceAll("%item%", item.name())
                            .replaceAll("%amount%", Long.toString(amount))
                            .replaceAll("%newamount%", Long.toString(otherAmount + amount))
            );
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

        String player = args[1].toLowerCase();

        if (!players.contains(player)) {
            return List.of();
        }

        List<String> items = PlayerData.getItemsAmount((OfflinePlayer) sender).entrySet()
                .parallelStream().filter(entry -> entry.getValue() != 0L).map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], items, result);
        }

        String itemName = args[2].toUpperCase();

        if (!items.contains(itemName)) {
            return List.of();
        }
        
        long stock = PlayerData.getItemAmount((OfflinePlayer) sender, Items.valueOf(itemName));

        if (stock < 1) {
            return List.of();
        }

        List<String> amountList = IntStream.iterate(1, n -> n * 10).limit(10).filter(n -> n < stock)
                .boxed().map(String::valueOf).collect(Collectors.toList());
        amountList.add(String.valueOf(stock));

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], amountList, result);
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
        return MESSAGE_CONFIG.getGiveDesc();
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

        if (sender.getName().equalsIgnoreCase(args[1])) {
            sender.sendMessage(MESSAGE_CONFIG.getCannotGiveYourself());
            return false;
        }

        // アイテムが登録されていない
        if (!CONFIG.getAllItems().contains(args[2].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoItemFound());
            return false;
        }

        Map<String, String> players = PlayerData.getPlayers();
        // プレイヤーがデータベースに登録されていない
        if (
            (!players.containsKey(args[1].toLowerCase()) &&
            !players.containsValue(args[1].toLowerCase())) ||
            (!players.containsKey(sender.getName().toLowerCase()) &&
            !players.containsValue(sender.getName().toLowerCase()))
        ){
            sender.sendMessage(MESSAGE_CONFIG.getNoPlayerFound());
            return false;
        }

        return true;
    }
}