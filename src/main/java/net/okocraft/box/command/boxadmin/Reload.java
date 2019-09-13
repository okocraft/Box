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

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

class Reload extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "reload";
    private static final int LEAST_ARG_LENGTH = 1;
    private static final String USAGE = "/boxadmin reload";

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }

        if (args.length == 1) {
            reloadConfig(sender);
        }

        if (args.length == 2) {
            String type = args[1].toLowerCase();
            switch (type) {
            case "listener":
                INSTANCE.registerEvents();
                break;
            case "config":
                reloadConfig(sender);
                break;
            }
        }

        sender.sendMessage(MESSAGE_CONFIG.getConfigReloaded());
        return true;
    }

    private void reloadConfig(CommandSender sender) {
        INSTANCE.reloadConfig();
        CONFIG.reload();
        MESSAGE_CONFIG.reload();
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
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public int getLeastArgLength() {
        return LEAST_ARG_LENGTH;
    }

    @Override
    public String getUsage() {
        return USAGE;
    }

    @Override
    public String getDescription() {
        return MESSAGE_CONFIG.getReloadDesc();
    }
}