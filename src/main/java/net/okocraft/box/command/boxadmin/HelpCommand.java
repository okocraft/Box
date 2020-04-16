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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.okocraft.box.command.BaseCommand;
import net.okocraft.box.util.OtherUtil;

class HelpCommand extends BaseAdminCommand {

    HelpCommand() {
        super(
            "help",
            "boxadmin.help",
            1,
            false,
            "/boxadmin help [page]",
            new String[0]
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        List<BaseCommand> registeredCommands = BoxAdminCommand.getInstance().getRegisteredCommands();
        int subCommandsSize = registeredCommands.size();
        int page = args.length > 1 ? OtherUtil.parseIntOrDefault(args[1], 1) : 1;
        int maxPage = subCommandsSize % 9 == 0 ? subCommandsSize / 9 : subCommandsSize / 9 + 1;
        page = Math.min(page, maxPage);

        messages.sendHelpHeader(sender);
        registeredCommands.stream().skip(9 * (page - 1)).limit(9).forEach(subCommand -> 
                messages.sendHelpFormat(sender, subCommand.getName(), subCommand.getDescription()));        
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        List<BaseCommand> registeredCommands = BoxAdminCommand.getInstance().getRegisteredCommands();
        int subCommandsSize = registeredCommands.size();
        int maxPage = subCommandsSize % 9 == 0 ? subCommandsSize / 9 : subCommandsSize / 9 + 1;
        List<String> pages = IntStream.rangeClosed(1, maxPage).boxed().map(String::valueOf).collect(Collectors.toList());
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], pages, result);
        }
        return result;
    }
}