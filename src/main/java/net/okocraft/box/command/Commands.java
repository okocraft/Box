/*
 * Box
 * Copyright (C) 2019 AKANE AKAGI
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

package net.okocraft.box.command;

import lombok.val;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

import org.bukkit.command.CommandSender;

import java.util.Optional;

@SuppressWarnings("unused")
public class Commands {
    private Database database;

    public Commands(Database database) {
        val instance = Box.getInstance();

        this.database = database;

        // コマンド登録
        Optional.ofNullable(instance.getCommand("box")).ifPresent(cmd ->
                cmd.setExecutor(new BoxCommand(this.database))
        );

        Optional.ofNullable(instance.getCommand("boxadmin")).ifPresent(cmd ->
                cmd.setExecutor(new BoxAdminCommand(this.database))
        );
    }

    /**
     * 文字列を検証してプレイヤー名か UUID か判定する。
     *
     * @param entry 検証する文字列
     *
     * @return UUID なら "uuid", さもなくば "player"
     */
    // TODO: 別クラスにする
    public static String checkEntryType(String entry) {
        return entry.matches("([a-z]|\\d){8}(-([a-z]|\\d){4}){3}-([a-z]|\\d){12}") ? "uuid" : "player";
    }
}
