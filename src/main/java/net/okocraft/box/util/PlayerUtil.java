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

package net.okocraft.box.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

/**
 * プレイヤー名やそのインスタンスを取り扱うツール群。
 *
 * @author akaregi
 * @since v1.1.0
 */
public class PlayerUtil {

    private static Box instance = Box.getInstance();
    private static Database database = instance.getDatabase();

    /**
     * 文字列を検証して UUID か Minecraft ID かを判定する。
     *
     * @param entry 検証する文字列
     *
     * @return UUID なら "uuid", Minecraft ID であれば "player" を返す。
     */
    public static String isUuidOrPlayer(String entry) {
        return entry.matches("([a-z]|\\d){8}(-([a-z]|\\d){4}){3}-([a-z]|\\d){12}") ? "uuid" : "player";
    }

    /**
     * データベースからUUIDを取得してそれを元にOfflinePlayerを取得する。プレイヤーが登録されていないときはコンソールに警告を出力する。
     * 
     * @param name 取得するプレイヤーの名前
     * 
     * @return OfflinePlayerインスタンス
     */
    public static OfflinePlayer getOfflinePlayer(String name) {
        String uuidString = database.get("uuid", name);
        if (uuidString.equals(":NOTHING")) {
            name = "";
            instance.getLog().warning(instance.getMessageConfig().getNoPlayerFound().replaceAll("%player%", name));
        }
        return Bukkit.getOfflinePlayer(UUID.fromString(uuidString));
    }
}