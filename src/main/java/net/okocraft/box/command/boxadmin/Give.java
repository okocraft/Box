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

import net.okocraft.box.util.OtherUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

class Give extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "give";
    private static final int LEAST_ARG_LENGTH = 3;
    private static final String USAGE = "/boxadmin give <player> <ITEM> [amount]";

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }
        String player = args[1].toLowerCase();
        String item = args[2].toUpperCase();
        long amount = args.length < 4 ? 1 : OtherUtil.parseLongOrDefault(args[3], 1);

        long currentAmount;

        try {
            currentAmount = Long.parseLong(DATABASE.get(item, player));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            sender.sendMessage(MESSAGE_CONFIG.getInvalidNumberFormat());
            return false;
        }

        String value = String.valueOf(currentAmount + amount);
        DATABASE.set(item, player, value);

        sender.sendMessage(
                MESSAGE_CONFIG.getSuccessGiveAdmin()
                        .replaceAll("%player%", player)
                        .replaceAll("%item%", item)
                        .replaceAll("%amount%", Long.toString(amount))
                        .replaceAll("%newamount%", value)
        );

        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> players = new ArrayList<>(DATABASE.getPlayersMap().values());

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], players, result);
        }

        if (!players.contains(args[1].toLowerCase())) {
            return List.of();
        }

        List<String> items = CONFIG.getAllItems();

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], items, result);
        }

        if (!items.contains(args[2].toUpperCase())) {
            return List.of();
        }

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], List.of("1", "10", "100", "1000", "10000"), result);
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
        return MESSAGE_CONFIG.getGiveAdminDesc();
    }

    @Override
    protected boolean validate(CommandSender sender, String[] args) {
        if (!super.validate(sender, args)) {
            return false;
        }

        if (!DATABASE.existPlayer(args[1].toLowerCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoPlayerFound());
            return false;
        }

        if (!CONFIG.getAllItems().contains(args[2].toUpperCase())) {
            sender.sendMessage(MESSAGE_CONFIG.getNoItemFound());
            return false;
        }

        return true;
    }
}