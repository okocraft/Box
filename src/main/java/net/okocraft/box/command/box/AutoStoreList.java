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

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.box.config.Categories;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.util.OtherUtil;

class AutoStoreList extends BoxSubCommand {

    AutoStoreList() {
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            messages.sendMessage(sender, "command.general.error.player-only");
            return false;
        }

        int maxLine = Categories.getInstance().getAllItems().size();
        int maxPage = maxLine % 9 == 0 ? maxLine / 9 : maxLine / 9 + 1;
        int page = Math.min(maxPage, (args.length >= 2 ? OtherUtil.parseIntOrDefault(args[1], 1) : 1));
        int currentLine = Math.min(maxLine, page * 9);

        messages.sendMessage(sender, "command.box.auto-store-list.info.header", Map.of(
                "%player%", sender.getName(),
                "%page%", String.valueOf(page),
                "%current-line%", String.valueOf(currentLine),
                "%max-line%", String.valueOf(maxLine))
        );
        PlayerData.getAutoStoreAll((OfflinePlayer) sender).entrySet().stream().skip((page - 1) * 9).limit(9)
                .forEach(entry ->
                        messages.sendMessage(sender, false, "command.box.auto-store-list.info.format", Map.of(
                                "%item%", entry.getKey(),
                                "%is-enabled%", entry.getValue().toString()
                        )));
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        int items = Categories.getInstance().getAllItems().size();
        int maxPage = items % 9 == 0 ? items / 9 : items / 9 + 1;
        List<String> pages = IntStream.rangeClosed(1, maxPage).boxed().map(String::valueOf)
                .collect(Collectors.toList());
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], pages, new ArrayList<>());
        }
        return List.of();
    }

    @Override
    public int getLeastArgLength() {
        return 2;
    }

    @Override
    public String getUsage() {
        return "/box autostorelist <page>";
    }
}