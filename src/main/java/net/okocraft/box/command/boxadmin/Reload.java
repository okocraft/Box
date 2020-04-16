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

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import net.okocraft.box.gui.CategorySelectorGUI;

class Reload extends BoxAdminSubCommand {

    Reload() {
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (args.length == 1) {
            config.reloadAllConfigs();
            CategorySelectorGUI.initGUI();
        }

        if (args.length == 2) {
            String type = args[1].toLowerCase(Locale.ROOT);
            switch (type) {
            case "listener":
                plugin.registerEvents();
                break;
            case "config":
                config.reloadAllConfigs();
                CategorySelectorGUI.initGUI();
                break;
            }
        }

        messages.sendMessage(sender, "command.box-admin.reload.info.success");
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        List<String> subCommands = List.of("listener", "config");

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], subCommands, result);
        }

        return result;
    }

    @Override
    public int getLeastArgLength() {
        return 1;
    }

    @Override
    public String getUsage() {
        return "/boxadmin reload";
    }

}