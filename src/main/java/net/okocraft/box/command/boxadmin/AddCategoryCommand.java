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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.box.listeners.GenerateCategory;

class AddCategoryCommand extends BaseAdminCommand {

    AddCategoryCommand() {
        super(
            "addcategory",
            "boxadmin.addcategory",
            5,
            true,
            "/boxadmin addcategory <category> <id> <displayName> <iconMaterial>",
            new String[] {"ac"}
        );
    }
    
    @Override
    public boolean runCommand(CommandSender sender, String[] args) {

        messages.sendChooseChest(sender);
        new GenerateCategory((Player) sender, args[1], args[2], args[3]);
        return true;
    }

        @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        switch (args.length) {
            case 2:
                return StringUtil.copyPartialMatches(args[2], List.of("<id>"), result);
            case 3:
                return StringUtil.copyPartialMatches(args[3], List.of("<display_name>"), result);
            case 4:
                List<String> items = Arrays.stream(Material.values())
                        .map(Material::name).collect(Collectors.toList());
                return StringUtil.copyPartialMatches(args[4], items, result);
            default:
                return result;
        }
    }
}