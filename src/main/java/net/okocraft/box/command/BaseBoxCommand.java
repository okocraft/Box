package net.okocraft.box.command;

import net.okocraft.box.util.OtherUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

public abstract class BaseBoxCommand implements BoxCommand {

    /**
     * コンストラクタ
     */
    protected BaseBoxCommand() {
        OtherUtil.registerPermission(getPermissionNode(), "box.*");
    }

    /**
     * 権限や引数の長さなどが基準を満たしているか確認する。
     *
     * @return 満たしていればtrue
     */
    protected boolean validate(CommandSender sender, String[] args) {
        if ((sender instanceof Player) && !sender.hasPermission(getPermissionNode())) {
            sender.sendMessage(MESSAGE_CONFIG.getPermissionDenied());
            return false;
        }

        if (args.length < getLeastArgLength()) {
            sender.sendMessage(MESSAGE_CONFIG.getNotEnoughArguments());
            return false;
        }

        return true;
    }
}