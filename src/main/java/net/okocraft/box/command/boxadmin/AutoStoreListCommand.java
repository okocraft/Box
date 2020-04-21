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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;


class AutoStoreListCommand extends BaseAdminCommand {

    AutoStoreListCommand() {
        super(
            "autostorelist",
            "boxadmin.autostorelist",
            3,
            false,
            "/boxadmin autostorelist <player> <page>",
            new String[] {"asl"}
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        String playerName = args[1].toLowerCase(Locale.ROOT);
        
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (!player.hasPlayedBefore() || player.getName() == null) {
            messages.sendPlayerNotFound(sender);
            return false;
        }

        Map<ItemStack, Boolean> autoStoreData = playerData.getAutoStoreAll(player);
        int maxLine = autoStoreData.size();
        int maxPage = maxLine % 9 == 0 ? maxLine / 9 : maxLine / 9 + 1;
        int page = Math.max(maxPage, (args.length >= 3 ? parseIntOrDefault(args[2], 1) : 1));
        int currentLine = Math.min(maxLine, page * 9);
        messages.sendAutoStoreListHeader(sender, player.getName(), page, currentLine, maxLine);
        playerData.getAutoStoreAll((OfflinePlayer) sender).entrySet().stream().skip((page - 1) * 9).limit(9)
                .forEach(entry -> messages.sendAutoStoreListFormat(sender, entry.getKey(), entry.getValue()));
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> players = playerData.getPlayers();

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], players, result);
        }

        String playerName = args[1].toLowerCase(Locale.ROOT);

        if (!players.contains(playerName)) {
            return List.of();
        }

        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        int items = playerData.getAutoStoreAll(offlinePlayer).size();
        int maxPage = items % 9 == 0 ? items / 9 : items / 9 + 1;
        List<String> pages = IntStream.rangeClosed(1, maxPage).boxed().map(String::valueOf).collect(Collectors.toList());
        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], pages, result);
        }
        return result;
    }
}