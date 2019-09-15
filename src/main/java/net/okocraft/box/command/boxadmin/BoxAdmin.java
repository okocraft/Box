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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import lombok.Getter;
import net.okocraft.box.command.BaseBoxCommand;
import net.okocraft.box.command.BoxCommand;
import org.jetbrains.annotations.NotNull;

public class BoxAdmin extends BaseBoxCommand implements CommandExecutor, TabCompleter {

    private static final String COMMAND_NAME = "boxadmin";
    private static final int LEAST_ARG_LENGTH = 1;
    private static final String USAGE = "/boxadmin <args...>";

    @Getter
    private Map<String, BoxCommand> subCommandMap;
    @Getter
    private final int subCommandMapSize;

    public BoxAdmin() {
        subCommandMap = new HashMap<>() {
            private static final long serialVersionUID = 1L;

            {
                Help help = new Help();
                put(help.getCommandName(), help);

                AddCategory addCategory = new AddCategory();
                put(addCategory.getCommandName(), addCategory);

                AutoStoreList autoStoreList = new AutoStoreList();
                put(autoStoreList.getCommandName(), autoStoreList);

                AutoStore autoStore = new AutoStore();
                put(autoStore.getCommandName(), autoStore);

                Give give = new Give();
                put(give.getCommandName(), give);

                Set set = new Set();
                put(set.getCommandName(), set);

                Take take = new Take();
                put(take.getCommandName(), take);

                Reload reload = new Reload();
                put(reload.getCommandName(), reload);
            }
        };

        subCommandMapSize = subCommandMap.size();

        Optional.ofNullable(INSTANCE.getCommand(getCommandName())).ifPresent(pluginCommand -> {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        });

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        BoxCommand subCommand = subCommandMap.get(args[0].toLowerCase());
        if (subCommand == null) {
            sender.sendMessage(MESSAGE_CONFIG.getNoParamExist());
            return false;
        }
        return subCommand.runCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return null;
        }

        List<String> permedSubCommands = subCommandMap.entrySet().stream()
                .filter(entry -> sender.hasPermission(entry.getValue().getPermissionNode()))
                .map(Map.Entry::getKey).collect(Collectors.toList());

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], permedSubCommands, new ArrayList<>());
        }

        BoxCommand subCommand = subCommandMap.get(args[0].toLowerCase());
        if (subCommand == null || !permedSubCommands.contains(subCommand.getCommandName())) {
            return List.of();
        }
        return subCommand.runTabComplete(sender, args);
    }

    @NotNull
    @Override
    public String getDescription() {
        return "";
    }

    @NotNull
    @Override
    public String getUsage() {
        return USAGE;
    }

    @NotNull
    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @NotNull
    @Override
    public String getPermissionNode() {
        return getCommandName();
    }

    @Override
    public int getLeastArgLength() {
        return LEAST_ARG_LENGTH;
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("BoxAdmin must be executed with sub commands.");
    }

    @NotNull
    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("Non subcommand could not be completed with this method.");
    }
}