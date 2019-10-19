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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import net.okocraft.box.command.BoxCommand;

public class BoxAdmin extends BoxCommand implements CommandExecutor, TabCompleter {

    static enum SubCommands {
        HELP(new Help()),
        ADD_CATEGORY(new AddCategory()),
        AUTO_STORE_LIST(new AutoStoreList()),
        AUTO_STORE(new AutoStore()),
        SET(new Set()),
        GIVE(new Give()),
        TAKE(new Take()),
        RELOAD(new Reload());

        private final BoxAdminSubCommand subCommand;

        private SubCommands(BoxAdminSubCommand subCommand) {
            this.subCommand = subCommand;
        }

        public BoxAdminSubCommand getSubCommand() {
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

        static SubCommands get(BoxAdminSubCommand BoxAdminSubCommand) {
            for (SubCommands subCommand : values()) {
                if (subCommand.getSubCommand() == BoxAdminSubCommand) {
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

    private static final BoxAdmin instance = new BoxAdmin();

    public static void load() {}

    static BoxAdmin getInstance() {
        return instance;
    }

    private BoxAdmin() {
        Optional.ofNullable(plugin.getCommand(getName())).ifPresent(pluginCommand -> {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            MESSAGES.sendMessage(sender, "command.general.error.not-enough-arguments");
            return false;
        }

        SubCommands subCommands = SubCommands.get(args[0]);
        if (subCommands == null) {
            MESSAGES.sendMessage(sender, "command.general.error.invalid-argument",
                    Map.of("%argument%", args[0]));
            return false;
        }

        BoxAdminSubCommand subCommand = subCommands.getSubCommand();

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
                .filter(subCommand -> subCommand.hasPermission(sender)).map(BoxAdminSubCommand::getName)
                .collect(Collectors.toList());

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], subCommands, new ArrayList<>());
        }

        BoxAdminSubCommand subCommand = SubCommands.get(args[0].toLowerCase(Locale.ROOT)).getSubCommand();
        if (subCommand == null || !subCommands.contains(subCommand.getName())) {
            return List.of();
        }
        return subCommand.runTabComplete(sender, args);
    }

    @Override
    public String getUsage() {
        return "/boxadmin <args...>";
    }

    @Override
    public int getLeastArgLength() {
        return 1;
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("BoxAdmin must be executed with sub commands.");
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("Non subcommand could not be completed with this method.");
    }
}