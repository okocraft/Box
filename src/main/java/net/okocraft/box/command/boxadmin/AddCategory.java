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

import net.okocraft.box.listeners.GenerateItemConfig;
import org.jetbrains.annotations.NotNull;

class AddCategory extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "addcategory";
    private static final int LEAST_ARG_LENGTH = 5;
    private static final String USAGE = "/boxadmin addcategory <category> <id> <displayName> <iconMaterial>";

    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        sender.sendMessage("[Box] チェストを選択してください");
        new GenerateItemConfig((Player) sender, args[1], args[2], args[3], args[4]);
        return true;
    }

    @NotNull
    @Override
    public List<String> runTabComplete(CommandSender sender, @NotNull String[] args) {
        List<String> result = new ArrayList<>();

        switch (args.length) {
        case 2:
            return StringUtil.copyPartialMatches(args[1], List.of("<category>"), result);
        case 3:
            return StringUtil.copyPartialMatches(args[2], List.of("<id>"), result);
        case 4:
            return StringUtil.copyPartialMatches(args[3], List.of("<display_name>"), result);
        case 5:
        List<String> items = Arrays.stream(Material.values())
                .map(Material::name).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[4], items, result);
        default:
            return result;
        }
    }

    @NotNull
    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public int getLeastArgLength() {
        return LEAST_ARG_LENGTH;
    }

    @NotNull
    @Override
    public String getUsage() {
        return USAGE;
    }

    @Override
    public String getDescription() {
        return MESSAGE_CONFIG.getAddCategoryDesc();
    }

    @Override
    protected boolean validate(CommandSender sender, @NotNull String[] args) {
        if (!super.validate(sender, args)) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(MESSAGE_CONFIG.getPlayerOnly());
            return false;
        }

        return true;
    }
}