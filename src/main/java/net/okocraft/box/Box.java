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
import net.okocraft.box.util.OtherUtil;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.okocraft.box.command.boxadmin.BoxAdmin;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.database.Sqlite;
import net.okocraft.box.gui.CategorySelectorGUI;
import net.okocraft.box.listeners.BoxStick;
import net.okocraft.box.listeners.EntityPickupItem;
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
    private net.okocraft.box.command.box.Box command;

    /**
     * 管理者コマンドクラス
     */
    @Getter
    private BoxAdmin adminCommand;


    /**
     * 経済
     */
    @Getter
    private Economy economy;

    public Box() {
        log      = getLogger();
        version  = getClass().getPackage().getImplementationVersion();
    }

    @Override
    public void onEnable() {
        // config
        generalConfig = new GeneralConfig();
        messageConfig = new MessageConfig();

        if (!setupEconomy()) {
            log.severe("Box failed to setup economy.");
        }

        registerEvents();
        OtherUtil.registerPermission("box.*");

        // Register commands
        command = new net.okocraft.box.command.box.Box();
        adminCommand = new BoxAdmin();

        PlayerData.loadOnlinePlayersData();

        // GO GO GO
        log.info(String.format("Box v%s has been enabled!", version));
    }

    @Override
    public void onDisable() {

        PlayerData.saveOnlinePlayersData();
        Sqlite.disconnect();

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
        new PlayerData(this);
        new EntityPickupItem(this);
        new BoxStick();
        new Replant();

        // GUI
        CategorySelectorGUI.restartListener();

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
        return economy != null;
	}
}
