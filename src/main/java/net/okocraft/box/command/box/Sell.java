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

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.milkbowl.vault.economy.Economy;
import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.OtherUtil;
import org.jetbrains.annotations.NotNull;

class Sell extends BaseSubCommand {

    private static final String COMMAND_NAME = "sell";
    private static final int LEAST_ARG_LENGTH = 2;
    private static final String USAGE = "/box sell <ITEM> [amount]";

    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        String itemName = args[1].toUpperCase();
        ItemStack item = Items.getItemStack(itemName);
        long amount = args.length == 2 ? 1L : Math.max(OtherUtil.parseLongOrDefault(args[2], 1L), 1L);
        long currentAmount = PlayerData.getItemAmount((OfflinePlayer) sender, item);

        if (currentAmount < amount) {
            amount = currentAmount;
        }

        if (amount == 0) {
            sender.sendMessage(MESSAGE_CONFIG.getNotEnoughStoredItem());
            return false;
        }

        Economy economy = INSTANCE.getEconomy();
        double price = CONFIG.getSellPrice().getOrDefault(itemName, 0D) * amount;
        
        PlayerData.setItemAmount((OfflinePlayer) sender, item, currentAmount - amount);
        economy.depositPlayer((OfflinePlayer) sender, price);
        
        double balance = economy.getBalance((OfflinePlayer) sender);
        sender.sendMessage(
                MESSAGE_CONFIG.getSuccessSell()
                        .replaceAll("%amount%", String.valueOf(amount))
                        .replaceAll("%item%", itemName)
                        .replaceAll("%price%", String.valueOf(price))
                        .replaceAll("%newamount%", String.valueOf(currentAmount - amount))
                        .replaceAll("%newbalance%", String.valueOf(Math.round((balance + price)*10)/10))
        );

        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, @NotNull String[] args) {
        List<String> result = new ArrayList<>();

        List<String> items = PlayerData.getItemsAmount((OfflinePlayer) sender).entrySet()
                .parallelStream().filter(entry -> entry.getValue() != 0L).map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], items, result);
        }

        String itemName = args[1].toUpperCase();
        if (!items.contains(itemName)) {
            return List.of();
        }

        String stock = Long.toString(PlayerData.getItemAmount((OfflinePlayer) sender, Items.getItemStack(itemName)));

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], List.of("1", stock), result);
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
        return MESSAGE_CONFIG.getSellDesc();
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

        if (INSTANCE.getEconomy() == null) {
            sender.sendMessage(MESSAGE_CONFIG.getEconomyIsNull());
            return false;
        }
        
        if (!CONFIG.getAllItems().contains(args[1].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoItemFound());
            return false;
        }

        return true;
    }
}