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

import net.okocraft.box.command.BoxCommand;
import net.okocraft.box.command.box.Box.SubCommands;

public abstract class BoxSubCommand extends BoxCommand {

    @Override
    public String getPermissionNode() {
        return "box." + SubCommands.get(this).toString();
    }

    @Override
    public String getDescription() {
        return messages.getMessage("command.box." + SubCommands.get(this).toString() + ".description");
    }
}