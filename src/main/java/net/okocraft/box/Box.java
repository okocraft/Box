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

package net.okocraft.box;

import java.util.logging.Logger;

import lombok.Getter;

import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.MessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import net.okocraft.box.command.Commands;
import net.okocraft.box.command.BoxTabCompleter;
import net.okocraft.box.database.Database;
import net.okocraft.box.listeners.EntityPickupItem;
import net.okocraft.box.listeners.GuiManager;
import net.okocraft.box.listeners.PlayerJoin;

/**
 * @author OKOCRAFT
 */
public class Box extends JavaPlugin {

    /**
     * ロガー。
     */
    @Getter
    private final Logger log;

    /**
     * プラグイン Box のインスタンス。
     */
    private static Box instance;

    /**
     * バージョン。
     */
    @Getter
    private final String version;

    /**
     * データベース。
     */
    @Getter
    private final Database database;

    /**
     * 通常設定
     */
    @Getter
    private final GeneralConfig generalConfig;

    /**
     * メッセージ設定
     */
    @Getter
    private final MessageConfig messageConfig;

    /**
     * GUIマネージャ
     */
    @Getter
    private final GuiManager guiManager;

    public Box() {
        log      = getLogger();
        version  = getClass().getPackage().getImplementationVersion();
        database = new Database(this);

        // config
        generalConfig = new GeneralConfig(database);
        messageConfig = new MessageConfig(this);

        // GUI
        guiManager    = new GuiManager(database, this);
    }

    @Override
    public void onEnable() {
        if (!database.connect(getDataFolder().getPath() + "/data.db")) {
            setEnabled(false);
            return;
        }

        // TODO: いる?
        // Implementation info
        log.info("Installed in : " + getDataFolder().getPath());
        log.info("Database file: " + database.getDBUrl());

        registerEvents();

        new Commands(database);
        new BoxTabCompleter(database);

        // CHANGED: バージョン表示追加
        log.info(String.format("Box v%s has been enabled!", version));
    }

    @Override
    public void onDisable() {
        database.dispose();

        unregisterEvents();
        cancelTasks();

        // CHANGED: バージョン表示追加
        log.info(String.format("Box v%s has been disabled!", version));
    }

    /**
     * このクラスのインスタンスを返す。
     *
     * @return インスタンス
     */
    public static Box getInstance() {
        if (instance == null) {
            instance = (Box) Bukkit.getPluginManager().getPlugin("Box");
        }

        return instance;
    }

    /**
     * イベントを Bukkit サーバに登録する。
     */
    public void registerEvents() {
        unregisterEvents();

        // Events should be registered in its own initializer
        new PlayerJoin(database, this);
        new EntityPickupItem(database, this);

        log.info("Events have been registered.");
    }

    /**
     * 登録したイベントを Bukkit サーバから削除する。
     */
    private void unregisterEvents() {
        HandlerList.unregisterAll(this);
    }

    /**
     * 登録したタスクを終了する。
     */
    private void cancelTasks() {
        Bukkit.getScheduler().cancelTasks(this);
    }
}
