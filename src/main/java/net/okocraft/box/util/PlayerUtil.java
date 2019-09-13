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

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.okocraft.box.Box;
import net.okocraft.box.database.PlayerData;

/**
 * プレイヤー名やそのインスタンスを取り扱うツール群。
 *
 * @author akaregi
 * @since v1.1.0
 */
public class PlayerUtil {

    private static final Box INSTANCE = Box.getInstance();
    private static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();

    /**
     * 文字列を検証して UUID かどうか判定する。
     *
     * @param entry 検証する文字列
     *
     * @return UUID なら {@code true}
     */
    public static boolean isUUID(String string) {
        try {
            UUID.fromString(string);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public static OfflinePlayer getOfflinePlayer(String uuidOrName) {
        if (isUUID(uuidOrName)) {
            return Bukkit.getOfflinePlayer(UUID.fromString(uuidOrName));
        } else {
            return Bukkit.getOfflinePlayer(uuidOrName);
        }
    }
    
    /**
     * データベースにプレイヤーが登録されていない時、senderにエラーメッセージを送信してfalseを返す。
     * 
     * @author akaregi
     * 
     * @since v1.1.0
     * 
     * @param sender
     * 
     * @return 登録されている時true、されていないならfalse
     */
    public static boolean existPlayer(CommandSender sender, String player) {
        Map<String, String> players = PlayerData.getPlayers();
        if (isUUID(player)) {
            if (players.containsKey(player)) {
                return true;
            }
        } else {
            if (players.containsValue(player)) {
                return true;
            }
        }
        
        sender.sendMessage(Box.getInstance().getMessageConfig().getNoPlayerFound());
        return false;
    }

    /**
     * プレイヤーに音声を流す。
     *
     * @param player プレイヤー
     * @param sound  流す音声
     */
    public static void playSound(Player player, Sound sound) {
        player.playSound(
                player.getLocation(),
                sound,
                SoundCategory.MASTER,
                CONFIG.getSoundPitch(),
                CONFIG.getSoundVolume()
        );
    }
}