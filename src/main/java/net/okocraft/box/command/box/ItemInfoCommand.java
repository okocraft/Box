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
import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.command.BaseCommand;

class ItemInfoCommand extends BaseCommand {

    ItemInfoCommand() {
        super(
            "iteminfo",
            "box.iteminfo",
            1,
            false,
            "/box iteminfo [ITEM]",
            new String[] {"iinfo"}
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        ItemStack item;
        if (args.length == 1 && sender instanceof Player) {
            item = ((Player) sender).getInventory().getItemInMainHand();
        } else if (args.length >= 2) {
            item = itemData.getItemStack(args[1].toUpperCase(Locale.ROOT));
        } else {
            messages.sendHoldItem(sender);
            return false;
        }

        if (item == null || !categories.getAllItems().contains(itemData.getName(item))) {
            messages.sendItemNotFound(sender);
            return false;
        }

        messages.sendItemInfo(sender, item);
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> items = itemData.getNames();
        items.retainAll(categories.getAllItems());
        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], itemData.getNames(), new ArrayList<>());
        }

        return List.of();
    }
}