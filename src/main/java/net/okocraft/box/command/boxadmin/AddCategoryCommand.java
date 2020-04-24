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
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

class AddCategoryCommand extends BaseAdminCommand {

    AddCategoryCommand() {
        super(
            "addcategory",
            "boxadmin.addcategory",
            4,
            true,
            "/boxadmin addcategory <category> <display-name> <icon>",
            new String[0]
        );
    }
    
    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Block lookingBlock = player.getTargetBlockExact(5);
        if (lookingBlock == null || lookingBlock.getType() != Material.CHEST) {
            messages.sendMessage(player, "command.boxadmin.add-category.error.not-chest-and-cancelled");
            return false;
        }

        Chest chestData = (Chest) lookingBlock.getState();
        ItemStack[] chestContents = chestData.getInventory().getContents();
        List<String> items = new ArrayList<>(Arrays.asList(chestContents))
                .stream()
                .filter(Objects::nonNull)
                .map(itemData::register)
                .filter(Objects::nonNull)
                .filter(itemName -> !itemName.isEmpty())
                .collect(Collectors.toList());

        categories.addCategory(args[1], args[2], items, args[3]);
        messages.sendMessage(player, "command.boxadmin.add-category.info.success");
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], List.of("<category>"), result);
        }

        if (args.length == 3) {
            return StringUtil.copyPartialMatches(args[2], List.of("<display-name>"), result);
        }

        if (args.length == 4) {
            return StringUtil.copyPartialMatches(args[3], itemData.getNames(), result);
        }

        return result;
    }
}