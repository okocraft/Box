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

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.command.BaseCommand;

class AutoStoreCommand extends BaseCommand {

    AutoStoreCommand() {
        super(
            "autostore",
            "box.autostore",
            2,
            true,
            "/box autostore < <ITEM> [true|false] | ALL <true|false> >",
            new String[0]
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {

        // autostore all <true|false>
        if (args[1].equalsIgnoreCase("ALL")) {
            if (args.length == 2) {
                messages.sendNotEnoughArguments(sender);
                return false;
            }

            // switchToがtrueでもfalseでもない場合
            if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) {
                messages.sendInvalidArgument(sender, args[2].toLowerCase(Locale.ROOT));
                return false;
            }

            boolean switchTo = args[2].equalsIgnoreCase("true");
            playerData.setAutoStoreAll((OfflinePlayer) sender, switchTo);
            messages.sendAutoStoreAll(sender, switchTo);
            return true;
        }

        // autostore Item [true|false]
        String itemName = args[1].toUpperCase(Locale.ROOT);
        if (!categories.getAllItems().contains(itemName)) {
            messages.sendItemNotFound(sender);
            return false;
        }
        ItemStack item = itemData.getItemStack(itemName);
        boolean now = playerData.getAutoStore((OfflinePlayer) sender, item);
        boolean switchTo = args.length > 2 ? args[2].equalsIgnoreCase("true") : !now;

        playerData.setAutoStore((OfflinePlayer) sender, item, switchTo);
        messages.sendAutoStore(sender, item, switchTo);
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> ItemStack = new ArrayList<>(categories.getAllItems());
        ItemStack.add("ALL");

        if (args.length == 2) {
            return StringUtil.copyPartialMatches(args[1], ItemStack, result);
        }

        String item = args[1].toUpperCase(Locale.ROOT);

        if (!ItemStack.contains(item)) {
            return List.of();
        }

        if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], List.of("true", "false"), result);
        }

        return result;
    }
}