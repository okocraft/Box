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
import org.jetbrains.annotations.NotNull;

class AutoStoreList extends BaseSubCommand {

    private static final String COMMAND_NAME = "autostorelist";
    private static final int LEAST_ARG_LENGTH = 2;
    private static final String USAGE = "/box autostorelist <page>";

    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!validate(sender, args)) {
            return false;
        }


        Set<String> allItems = CONFIG.getAllItems();

        int maxLine = allItems.size();
        int maxPage = maxLine % 9 == 0 ? maxLine / 9 : maxLine / 9 + 1;
        int page = Math.min(maxPage, (args.length >= 2 ? OtherUtil.parseIntOrDefault(args[1], 1) : 1));
        int currentLine = Math.min(maxLine, page * 9);

        sender.sendMessage(MESSAGE_CONFIG.getAutoStoreListHeader()
                .replaceAll("%player%", sender.getName().toLowerCase()).replaceAll("%page%", String.valueOf(page))
                .replaceAll("%currentLine%", String.valueOf(currentLine))
                .replaceAll("%maxLine%", String.valueOf(maxLine)));

        PlayerData.getAutoStoreAll((OfflinePlayer) sender).entrySet().stream().skip((page - 1) * 9).limit(9).forEach(
                entry -> sender.sendMessage(MESSAGE_CONFIG.getAutoStoreListFormat().replaceAll("%item%", entry.getKey())
                        .replaceAll("%isEnabled%", Boolean.toString(entry.getValue()))
                        .replaceAll("%currentLine%", String.valueOf(currentLine))
                        .replaceAll("%maxLine%", String.valueOf(maxLine))));

        return true;
    }

    @NotNull
    @Override
    public List<String> runTabComplete(CommandSender sender, @NotNull String[] args) {
        List<String> result = new ArrayList<>();

        int items = CONFIG.getAllItems().size();
        int maxPage = items % 9 == 0 ? items / 9 : items / 9 + 1;
        List<String> pages = IntStream.rangeClosed(1, maxPage).boxed().map(String::valueOf)
                .collect(Collectors.toList());
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], pages, result);
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
        return MESSAGE_CONFIG.getAutoStoreListDesc();
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

        return true;
    }
}