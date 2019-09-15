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
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

class SellPrice extends BaseSubCommand {

    private static final String COMMAND_NAME = "sellprice";
    private static final int LEAST_ARG_LENGTH = 2;
    private static final String USAGE = "/box sellprice <ITEM>";

    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        String item = args[1].toUpperCase();
        double price = Optional.ofNullable(CONFIG.getSellPrice().get(item)).orElse(0D);
        sender.sendMessage(
                MESSAGE_CONFIG.getSellPriceFormat()
                        .replaceAll("%item%", item)
                        .replaceAll("%price%", String.valueOf(price))
        );

        return true;
    }

    @NotNull
    @Override
    public List<String> runTabComplete(CommandSender sender, @NotNull String[] args) {
        List<String> result = new ArrayList<>();

        List<String> items = new ArrayList<>(CONFIG.getSellPrice().keySet());

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], items, result);
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
        return MESSAGE_CONFIG.getSellPriceDesc();
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