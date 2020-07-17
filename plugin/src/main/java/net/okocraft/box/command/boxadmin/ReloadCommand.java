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

import org.bukkit.command.CommandSender;

class ReloadCommand extends BaseAdminCommand {

    ReloadCommand() {
        super(
            "reload",
            "boxadmin.reload",
            1,
            false,
            "/boxadmin reload",
            new String[0]
        );
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        plugin.getAPI().reloadAllConfigs();
        messages.sendReloadSuccess(sender);
        return true;
    }
}