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

import net.okocraft.box.util.CraftRecipes;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.okocraft.box.command.box.BoxCommand;
import net.okocraft.box.command.boxadmin.BoxAdminCommand;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.database.Sqlite;
import net.okocraft.box.listeners.BoxStick;
import net.okocraft.box.listeners.EntityPickupItem;
import net.okocraft.box.listeners.PlayerListener;
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
     * 経済
     */
    private Economy economy;

    private BoxAPI api;

    @Override
    public void onEnable() {
        instance = this;
        economy = provideEconomy();

        this.api = new BoxAPI();

        registerEvents();

        new BoxCommand();
        new BoxAdminCommand();

        PlayerData.loadOnlinePlayersData();

        // Load static class CraftRecipes
        CraftRecipes.load();

        // GO GO GO
        getLogger().info(String.format("Box v%s has been enabled!", getVersion()));
    }

    @Override
    public void onDisable() {

        PlayerData.saveOnlinePlayersData();
        Sqlite.disconnect();

        getLogger().info(String.format("Box v%s has been disabled!", getVersion()));
    }

    /**
     * このクラスのインスタンスを返す。
     *
     * @return インスタンス
     * 
     * @throws IllegalStateException プラグインがまだロードされていないのに呼び出されたらスローされる。
     * @throws ClassCastException 同名の別プラグインがロードされている状態で呼び出されたらスローされる。
     */
    public static Box getInstance() throws IllegalStateException, ClassCastException {
        if (instance == null) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("Box");
            if (plugin == null) {
                throw new IllegalStateException("The plugin Box is not loaded yet.");
            }
            instance = (Box) plugin;
        }

        return instance;
    }

    public String getVersion() {
        return getDescription().getVersion();
    }

    /**
     * イベントを Bukkit サーバに登録する。
     */
    private void registerEvents() {

        // Events should be registered in its own initializer
        new PlayerListener().start();
        new PlayerData(this);
        new EntityPickupItem(this);
        new BoxStick();
        new Replant();

        getLogger().info("Events have been registered.");
    }

    public BoxAPI getAPI() {
        return api;
    }

    /**
     * Economyクラスのインスタンスをサービスプロバイダーから取得する
     *
     * @return Economyのインスタンス
     * 
     * @throws IllegalStateException Vaultがロードされていない時
     * @throws IllegalStateException EconomyクラスがServiceProviderに登録されていない時
     */
    private Economy provideEconomy() throws IllegalStateException {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            throw new IllegalStateException("Vault was not found.");
        }
        
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            throw new IllegalStateException("Economy is not registered on service provider.");
        }
        return rsp.getProvider();
    }


    /**
     * Economyクラスのインスタンスを取得する
     *
     * @return Economyのインスタンス
     * 
     * @throws IllegalStateException Economyクラスのインスタンスがまだロードされていない時
     */
    public Economy getEconomy() {
        if (economy == null) {
            throw new IllegalStateException("Economy is not setup yet.");
        }
        return economy;
    }
}
