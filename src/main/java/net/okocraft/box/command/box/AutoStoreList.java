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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.OtherUtil;

class AutoStoreList extends BaseSubCommand {

    private static final String COMMAND_NAME = "autostorelist";
    private static final int LEAST_ARG_LENGTH = 2;
    private static final String USAGE = "/box autostorelist <page>";

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        int index = args.length >= 2 ? OtherUtil.parseIntOrDefault(args[1], 1) : 1;

        Set<String> allItems = CONFIG.getAllItems();
        
        int maxLine = allItems.size();
        int currentLine = Math.min(maxLine, index * 8);

        sender.sendMessage(
                MESSAGE_CONFIG.getAutoStoreListHeader()
                        .replaceAll("%player%", sender.getName().toLowerCase())
                        .replaceAll("%page%", String.valueOf(index))
                        .replaceAll("%currentLine%", String.valueOf(currentLine))
                        .replaceAll("%maxLine%", String.valueOf(maxLine))
        );

        PlayerData.getAutoStoreAll((OfflinePlayer) sender).forEach((item, value) -> 
                sender.sendMessage(
                        MESSAGE_CONFIG.getAutoStoreListFormat()
                                .replaceAll("%item%", item)
                                .replaceAll("%isEnabled%", Boolean.toString(value))
                                .replaceAll("%currentLine%", String.valueOf(currentLine))
                                .replaceAll("%maxLine%", String.valueOf(maxLine))
                )
        );

        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        int items = CONFIG.getAllItems().size();
        int maxPage = items % 8 == 0 ? items / 8 : items / 8 + 1;
        List<String> pages  = IntStream.rangeClosed(1, maxPage).boxed().map(String::valueOf).collect(Collectors.toList());
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], pages, result);
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
        return MESSAGE_CONFIG.getAutoStoreListDesc();
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

        return true;
    }
}