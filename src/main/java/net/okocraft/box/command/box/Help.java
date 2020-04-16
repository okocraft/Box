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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.okocraft.box.command.box.Box.SubCommands;
import net.okocraft.box.util.OtherUtil;

class Help extends BoxSubCommand {

    Help() {
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        int subCommandsSize = SubCommands.values().length;
        int page = args.length > 1 ? OtherUtil.parseIntOrDefault(args[1], 1) : 1;
        int maxPage = subCommandsSize % 9 == 0 ? subCommandsSize / 9 : subCommandsSize / 9 + 1;
        page = Math.min(page, maxPage);

        messages.sendMessage(sender, "command.box.help.info.header");
        Arrays.stream(SubCommands.values()).skip(9 * (page - 1)).limit(9).map(SubCommands::getSubCommand).forEach(subCommand -> 
                messages.sendMessage(sender, false, "command.box.help.info.format", Map.of("%command%", subCommand.getName(), "%description%", subCommand.getDescription())));
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        int subCommandsSize = SubCommands.values().length;
        int maxPage = subCommandsSize % 9 == 0 ? subCommandsSize / 9 : subCommandsSize / 9 + 1;
        List<String> pages = IntStream.rangeClosed(1, maxPage).boxed().map(String::valueOf).collect(Collectors.toList());
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], pages, result);
        }
        return result;
    }

    @Override
    public int getLeastArgLength() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "/box help [page]";
    }
}