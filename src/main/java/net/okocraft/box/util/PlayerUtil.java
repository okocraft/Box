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
import org.jetbrains.annotations.Nullable;

/**
 * プレイヤー名やそのインスタンスを取り扱うツール群。
 *
 * @author akaregi
 * @since v1.1.0
 */
public final class PlayerUtil {

    @Nullable
    private static final Box plugin = Box.getInstance();

    private PlayerUtil() {
    }

    /**
     * 文字列を検証して UUID かどうか判定する。
     *
     * @param entry 検証する文字列
     * @return UUID なら {@code true}
     */
    public static boolean isUUID(String string) {
        return string.matches("^(\\w{4})\\1-\\1-\\1-\\1-\\1\\1\\1$");
    }

    @SuppressWarnings("deprecation")
    public static OfflinePlayer getOfflinePlayer(String uuidOrName) {
        if (isUUID(uuidOrName)) {
            return Bukkit.getOfflinePlayer(UUID.fromString(uuidOrName));
        } else {
            return Bukkit.getOfflinePlayer(uuidOrName);
        }
    }
}