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
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import lombok.val;
import net.okocraft.box.Box;
import net.okocraft.box.database.Database;

/**
 * プレイヤー名やそのインスタンスを取り扱うツール群。
 *
 * @author akaregi
 * @since v1.1.0
 */
public class PlayerUtil {

    private static final Box INSTANCE = Box.getInstance();
    private static final Database DATABASE = INSTANCE.getDatabase();
    private static final GeneralConfig CONFIG = INSTANCE.getGeneralConfig();

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
        String uuidString = DATABASE.get("uuid", name);
        if (uuidString.equals(":NOTHING")) {
            name = "";
            INSTANCE.getLog().warning(INSTANCE.getMessageConfig().getNoPlayerFound().replaceAll("%player%", name));
        }
        return Bukkit.getOfflinePlayer(UUID.fromString(uuidString));
    }
    
    /**
     * データベースにプレイヤーが登録されていない時、senderにエラーメッセージを送信してtrueを返す。
     * 
     * @author akaregi
     * 
     * @since v1.1.0
     * 
     * @param sender 登録されているかされてないか判定する人
     * 
     * @return 登録されていない時true されているならfalse
     */
    public static boolean notExistPlayer(CommandSender sender) {
        val player = ((Player) sender).getUniqueId().toString();

        if (!DATABASE.existPlayer(player)) {
            sender.sendMessage(Box.getInstance().getMessageConfig().getNoPlayerFound());

            return true;
        }

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