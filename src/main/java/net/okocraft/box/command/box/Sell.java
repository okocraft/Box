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

import net.milkbowl.vault.economy.Economy;
import net.okocraft.box.util.OtherUtil;
import net.okocraft.box.util.PlayerUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class Sell extends BaseSubCommand {

    private static final String COMMAND_NAME = "sell";
    private static final int LEAST_ARG_LENGTH = 2;
    private static final String USAGE = "/box sell <ITEM> [amount]";

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        String senderName = sender.getName().toLowerCase();
        String itemName = args[1].toUpperCase();

        long amount = args.length == 2 ? 1L : OtherUtil.parseLongOrDefault(args[2], 1L);
        if (amount <= 0) amount = 1L;

        long currentAmount = OtherUtil.parseLongOrDefault(DATABASE.get(itemName, senderName), Long.MIN_VALUE);

        if (currentAmount == Long.MIN_VALUE) {
            sender.sendMessage(
                    MESSAGE_CONFIG.getDatabaseInvalidValue()
            );
            return false;
        }

        if (currentAmount < amount) {
            amount = currentAmount;
        }

        if (amount == 0) {
            sender.sendMessage(MESSAGE_CONFIG.getNotEnoughStoredItem());
            return false;
        }

        Economy economy = INSTANCE.getEconomy();
        double price = CONFIG.getSellPrice().get(itemName) * amount;

        DATABASE.set(itemName, senderName, String.valueOf(currentAmount - amount));
        economy.depositPlayer((OfflinePlayer) sender, price);

        double balance = economy.getBalance((OfflinePlayer) sender);
        sender.sendMessage(
                MESSAGE_CONFIG.getSuccessSell()
                        .replaceAll("%amount%", String.valueOf(amount))
                        .replaceAll("%item%", itemName)
                        .replaceAll("%price%", String.valueOf(price))
                        .replaceAll("%newamount%", String.valueOf(currentAmount - amount))
                        .replaceAll("%newbalance%", String.valueOf(Math.round((balance + price) * 10) / 10))
        );

        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> items = DATABASE.getMultiValue(new ArrayList<>(CONFIG.getSellPrice().keySet()), sender.getName().toLowerCase())
                .entrySet().stream().filter(entry -> !entry.getValue().equals("0"))
                .map(Map.Entry::getKey).map(String::toUpperCase).collect(Collectors.toList());

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], items, result);
        }

        if (!items.contains(args[1].toUpperCase())) {
            return List.of();
        }

        String stock = DATABASE.get(args[1].toUpperCase(), sender.getName().toLowerCase());

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], List.of("1", stock), result);
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
        return MESSAGE_CONFIG.getSellDesc();
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

        if (INSTANCE.getEconomy() == null) {
            sender.sendMessage(MESSAGE_CONFIG.getEconomyIsNull());
            return false;
        }

        if (!CONFIG.getAllItems().contains(args[1].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoItemFound());
            return false;
        }

        if (PlayerUtil.notExistPlayer(sender)) {
            sender.sendMessage(MESSAGE_CONFIG.getNoPlayerFound());
            return false;
        }

        return true;
    }
}