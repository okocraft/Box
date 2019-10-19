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

package net.okocraft.box.command;

import java.util.List;
import java.util.Locale;

import org.bukkit.command.CommandSender;

import net.okocraft.box.Box;
import net.okocraft.box.config.Messages;

public abstract class BoxCommand {

    protected static final Box plugin = Box.getInstance();
    protected static final Messages MESSAGES = Messages.getInstance();

    /**
     * コンストラクタ
     */
    protected BoxCommand() {
    }

    /**
     * 各コマンドの処理
     *
     * @param sender コマンドの実行者
     * @param args   引数
     * @return コマンドが成功したらtrue
     */
    public abstract boolean runCommand(CommandSender sender, String[] args);

    /**
     * 各コマンドのタブ補完の処理
     *
     * @param sender コマンドの実行者
     * @param args   引数
     * @return その時のタブ補完のリスト
     */
    public abstract List<String> runTabComplete(CommandSender sender, String[] args);

    /**
     * コマンドの名前を取得する。
     *
     * @return コマンドの名前
     */
    public String getName() {
        return this.getClass().getSimpleName().toLowerCase(Locale.ROOT);
    }

    /**
     * このコマンドの権限を取得する。
     *
     * @return 権限
     */
    public String getPermissionNode() {
        return getName();
    }

    /**
     * 最低限必要な引数の長さを取得する。
     *
     * @return 最低限の引数の長さ
     */
    public abstract int getLeastArgLength();

    /**
     * コマンドの引数の内容を取得する。例: "/box autostoreList [page]"
     *
     * @return 引数の内容
     */
    public abstract String getUsage();

    /**
     * コマンドの説明を取得する。例: "アイテムの自動収納の設定をリストにして表示する。"
     *
     * @return コマンドの説明
     */
    public String getDescription() {
        return MESSAGES.getMessage("command." + getName() + ".desctiption");
    }

    /**
     * このコマンドを使う権限があるか調べる。
     * 
     * @param sender
     * @return 権限があればtrue なければfalse
     * @see CommandSender#hasPermission(String)
     */
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode());
    }
}