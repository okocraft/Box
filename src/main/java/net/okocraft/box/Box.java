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

package net.okocraft.box;

import java.util.logging.Logger;

import lombok.Getter;

import net.okocraft.box.util.GeneralConfig;
import net.okocraft.box.util.MessageConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.okocraft.box.command.box.BoxCommand;
import net.okocraft.box.command.boxadmin.BoxAdminCommand;
import net.okocraft.box.database.Database;
import net.okocraft.box.gui.CategorySelectorGUI;
import net.okocraft.box.listeners.EntityPickupItem;
import net.okocraft.box.listeners.PlayerJoin;
import net.okocraft.box.listeners.Replant;

/**
 * @author OKOCRAFT
 */
public class Box extends JavaPlugin {
    /**
     * プラグイン Box のインスタンス。
     */
    private static Box instance;

    /**
     * ロガー。
     */
    @Getter
    private final Logger log;

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
    private GeneralConfig generalConfig;

    /**
     * メッセージ設定
     */
    @Getter
    private MessageConfig messageConfig;

    /**
     * コマンドクラス
     */
    @Getter
    private BoxCommand command;

    /**
     * 管理者コマンドクラス
     */
    @Getter
    private BoxAdminCommand adminCommand;


    /**
     * 経済
     */
    @Getter
    private Economy economy;

    public Box() {
        log      = getLogger();
        version  = getClass().getPackage().getImplementationVersion();
        database = new Database(this);
    }

    @Override
    public void onEnable() {
        // config
        generalConfig = new GeneralConfig(database);
        messageConfig = new MessageConfig();

        if (!setupEconomy()) {
            log.severe("Box failed to setup economy.");
        }

        // Database
        if (!database.connect(getDataFolder().getPath() + "/data.db")) {
            setEnabled(false);
            return;
        }

        // Implementation info
        log.info("Installed in : " + getDataFolder().getPath());
        log.info("Database file: " + database.getDBUrl());

        registerEvents();

        // Register commands
        command = new BoxCommand();
        adminCommand = new BoxAdminCommand();

        // GO GO GO
        log.info(String.format("Box v%s has been enabled!", version));
    }

    @Override
    public void onDisable() {
        database.dispose();

        unregisterEvents();
        cancelTasks();

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
        new Replant();

        // GUI
        CategorySelectorGUI.startListener();

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

    /**
     * economyをセットする。
     * 
     * @return 成功したらtrue　失敗したらfalse
     */
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            log.severe("Vault was not found.");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
	}
}
