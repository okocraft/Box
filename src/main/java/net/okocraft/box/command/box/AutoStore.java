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
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import net.okocraft.box.config.Categories;
import net.okocraft.box.database.Items;
import net.okocraft.box.database.PlayerData;

class AutoStore extends BoxSubCommand {

    AutoStore() {
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            messages.sendMessage(sender, "command.general.error.player-only");
            return false;
        }

        // autostore all <true|false>
        if (args[1].equalsIgnoreCase("ALL")) {
            if (args.length == 2) {
                messages.sendMessage(sender, "command.general.error.not-enough-arguments");
                return false;
            }

            // switchToがtrueでもfalseでもない場合
            if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false")) {
                messages.sendMessage(sender, "command.general.error.invalid-argument",
                        Map.of("%argument%", args[2].toLowerCase(Locale.ROOT)));
                return false;
            }

            boolean switchTo = args[2].equalsIgnoreCase("true");
            if (PlayerData.setAutoStoreAll((OfflinePlayer) sender, switchTo)) {
                messages.sendMessage(sender, "command.box.auto-store.info.changed-all",
                        Map.of("%is-enabled%", String.valueOf(switchTo)));
                return true;
            } else {
                messages.sendMessage(sender, "command.general.error.unknown-exception");
                return false;
            }
        }

        // autostore Item [true|false]
        String itemName = args[1].toUpperCase(Locale.ROOT);
        if (!Categories.getInstance().getAllItems().contains(itemName)) {
            messages.sendMessage(sender, "command.general.error.item-not-found");
            return false;
        }
        ItemStack item = Items.getItemStack(itemName);
        boolean now = PlayerData.getAutoStore((OfflinePlayer) sender, item);
        boolean switchTo = args.length > 2 ? args[2].equalsIgnoreCase("true") : !now;

        if (PlayerData.setAutoStore((OfflinePlayer) sender, item, switchTo)) {
            messages.sendMessage(sender, "command.box.auto-store.info.changed",
                    Map.of("%item%", itemName, "%is-enabled%", String.valueOf(switchTo)));
            return true;
        } else {
            messages.sendMessage(sender, "command.general.error.unknown-exception");
            return false;
        }
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();

        List<String> ItemStack = new ArrayList<>(Categories.getInstance().getAllItems());
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

    @Override
    public int getLeastArgLength() {
        return 2;
    }

    @Override
    public String getUsage() {
        return "/box autostore < <ITEM> [true|false] | ALL <true|false> >";
    }
}