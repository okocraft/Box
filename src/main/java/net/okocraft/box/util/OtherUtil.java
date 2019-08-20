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

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

public class OtherUtil {

    /**
     * numberを解析してint型にして返す。numberのフォーマットがintではないときはdefを返す。
     * 
     * @author LazyGon
     * @since v1.1.0
     * 
     * @param number 解析する文字列
     * @param def 解析に失敗したときに返す数字
     * 
     * @return int型の数字。
     */
    public static int parseIntOrDefault(@Nonnull String number, int def) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException exception) {
            return def;
        }
    }

    /**
     * numberを解析してlong型にして返す。numberのフォーマットがlongではないときはdefを返す。
     * 
     * @author LazyGon
     * @since v1.1.0
     * 
     * @param number 解析する文字列
     * @param def 解析に失敗したときに返す数字
     * 
     * @return long型の数字。
     */
    public static long parseLongOrDefault(@Nonnull String number, long def) {
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException exception) {
            return def;
        }
    }

    /**
     * パーミッションを登録する。
     * 
     * @param permName 登録する権限の文字列
     * @param parentPermName {@code permName} の親権限の文字列
     */
    public static void registerPermission(String permName, @Nullable String parentPermName) {
        PluginManager pm = Bukkit.getPluginManager();
        Optional<Permission> optionalPerm = Optional.ofNullable(pm.getPermission(permName));

        if (!optionalPerm.isPresent()) {
            Permission perm = new Permission(permName);

            Optional.ofNullable(parentPermName).ifPresent(_parentPermName -> Optional.ofNullable(pm.getPermission(_parentPermName)).ifPresent(parentPerm -> perm.addParent(parentPerm, true)));

            pm.getPermissions().add(perm);
        }
    }

    /**
     * パーミッションを登録する。
     * 
     * @param permName 登録する権限の文字列
     */
    public static void registerPermission(String permName) {
        registerPermission(permName, null);
    }

    public static void debugMsg(String message) {
        Optional.ofNullable(Bukkit.getPlayer("lazy_gon")).ifPresent(lazy -> lazy.sendMessage(message));
    }
}