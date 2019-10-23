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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.box.command.BoxCommand;
import net.okocraft.box.gui.CategorySelectorGUI;

public class Box extends BoxCommand implements CommandExecutor, TabCompleter {

    static enum SubCommands {
        VERSION(new Version()), HELP(new Help()), AUTO_STORE_LIST(new AutoStoreList()), AUTO_STORE(new AutoStore()),
        STICK(new Stick()), GIVE(new Give());

        private final BoxSubCommand subCommand;

        private SubCommands(BoxSubCommand subCommand) {
            this.subCommand = subCommand;
        }

        public BoxSubCommand getSubCommand() {
            return subCommand;
        }

        public static SubCommands get(String name) {
            for (SubCommands subCommand : values()) {
                if (subCommand.getSubCommand().getName().equalsIgnoreCase(name)) {
                    return subCommand;
                }
            }
            return null;
        }

        static SubCommands get(BoxSubCommand boxSubCommand) {
            for (SubCommands subCommand : values()) {
                if (subCommand.getSubCommand() == boxSubCommand) {
                    return subCommand;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return name().replaceAll("_", "-").toLowerCase(Locale.ROOT);
        }
    }

    private static final Box instance = new Box();

    private Box() {
        Optional.ofNullable(plugin.getCommand(getName())).ifPresent(pluginCommand -> {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        });
    }

    public static void load() {}

    static Box getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return runCommand(sender, args);
        }

        SubCommands subCommands = SubCommands.get(args[0]);
        if (subCommands == null) {
            MESSAGES.sendMessage(sender, "command.general.error.invalid-argument",
                    Map.of("%argument%", args[0]));
            return false;
        }

        BoxSubCommand subCommand = subCommands.getSubCommand();

        if (!subCommand.hasPermission(sender)) {
            MESSAGES.sendMessage(sender, "command.general.error.no-permission", Map.of("%permission%", subCommand.getPermissionNode()));
            return false;
        }

        if (subCommand.getLeastArgLength() > args.length) {
            MESSAGES.sendMessage(sender, "command.general.error.not-enough-arguments");
            return false;
        }

        return subCommand.runCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> subCommands = Arrays.stream(SubCommands.values()).map(SubCommands::getSubCommand)
                .filter(subCommand -> subCommand.hasPermission(sender)).map(BoxSubCommand::getName)
                .collect(Collectors.toList());

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], subCommands, new ArrayList<>());
        }

        String subCommandName = args[0].toLowerCase(Locale.ROOT);
        if (!subCommands.contains(subCommandName)) {
            return List.of();
        }

        BoxSubCommand subCommand = SubCommands.get(subCommandName).getSubCommand();
        if (subCommand == null || !subCommands.contains(subCommand.getName())) {
            return List.of();
        }
        return subCommand.runTabComplete(sender, args);
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            MESSAGES.sendMessage(sender, "command.general.error.player-only");
            return false;
        }
        ((Player) sender).openInventory(CategorySelectorGUI.GUI);
        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("Non subcommand could not be completed with this method.");
    }

    @Override
    public String getUsage() {
        return "/box [args...]";
    }

    @Override
    public int getLeastArgLength() {
        return 0;
    }
}