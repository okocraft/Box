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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Optional;

import lombok.Getter;

import lombok.val;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.okocraft.box.Box;
import org.jetbrains.annotations.NotNull;

/**
 * メッセージ設定クラス
 *
 * @author akaregi
 * @since v1.1.0
 */
public class MessageConfig {
    @NotNull
    private final CustomConfig configFile;

    /**
     * プラグインのデータフォルダ(plugins/Box/)に存在する messages.yml 。
     */
    private FileConfiguration config;

    // Plugin
    private String prefix;
    @Getter
    private String configReloaded;
    @Getter
    private String versionInfo;

    // Error
    @Getter
    private String notEnoughArguments;
    @Getter
    private String notEnoughStoredItem;
    @Getter
    private String notEnoughMoney;
    @Getter
    private String noPlayerFound;
    @Getter
    private String noItemFound;
    @Getter
    private String noParamExist;
    @Getter
    private String invalidArguments;
    @Getter
    private String invalidNumberFormat;
    @Getter
    private String errorOccurred;
    @Getter
    private String errorOccurredOnGUI;
    @Getter
    private String cannotGiveYourself;
    @Getter
    private String errorFetchCategoryName;
    @Getter
    private String errorFetchItemConfig;
    @Getter
    private String permissionDenied;
    @Getter
    private String playerOnly;
    @Getter
    private String economyIsNull;
    // @Getter
    // private String errorFetchDisplayName;

    // Command
    @Getter
    private String commandHelpHeader;
    @Getter
    private String adminCommandHelpHeader;
    @Getter
    private String disabledWorld;
    @Getter
    private String successSet;
    @Getter
    private String successTake;
    @Getter
    private String successGive;
    @Getter
    private String successReceive;
    @Getter
    private String successSell;
    @Getter
    private String successGiveAdmin;

    // command description
    @Getter
    private String boxDesc;
    @Getter
    private String autoStoreDesc;
    @Getter
    private String autoStoreListDesc;
    @Getter
    private String giveDesc;
    @Getter
    private String sellDesc;
    @Getter
    private String sellPriceDesc;
    @Getter
    private String sellPriceListDesc;
    @Getter
    private String versionDesc;
    @Getter
    private String helpDesc;

    // admin command description
    @Getter
    private String addCategoryDesc;
    @Getter
    private String autoStoreAdminDesc;
    @Getter
    private String autoStoreListAdminDesc;
    @Getter
    private String giveAdminDesc;
    @Getter
    private String setDesc;
    @Getter
    private String takeDesc;
    @Getter
    private String reloadDesc;
    @Getter
    private String helpAdminDesc;

    // Price
    @Getter
    private String sellPriceListHeader;
    @Getter
    private String sellPriceFormat;
    @Getter
    private String sellPriceListFormat;

    // AutoStore
    @Getter
    private String autoStoreEnabled;
    @Getter
    private String autoStoreDisabled;
    @Getter
    private String autoStoreSettingChanged;
    @Getter
    private String autoStoreSettingChangedAll;
    @Getter
    private String autoStoreListHeader;
    @Getter
    private String autoStoreListFormat;

    // Database
    @Getter
    private String databaseConnectionReset;
    @Getter
    private String databaseInvalidValue;
    @Getter
    private String databaseNoColumn;
    @Getter
    private String databasePlayerAdded;
    @Getter
    private String databasePlayerRemoved;

    // Map
    @Getter
    private String mapPlayersRecord;
    @Getter
    private String mapColumnsList;
    @Getter
    private String mapFormat;

    public MessageConfig() {
        val plugin = Box.getInstance();

        configFile = new CustomConfig(plugin, "messages.yml");
        configFile.initConfig();
        configFile.saveDefaultConfig();

        config = configFile.getConfig();

        initConfig();
    }

    /**
     * 設定を再読込する。
     *
     * @author akaregi
     * @since v1.1.0
     */
    public void reload() {
        configFile.initConfig();
        config = configFile.getConfig();

        initConfig();
    }

    /**
     * 設定を初期化する。
     *
     * @author akaregi
     * @since v1.1.0
     */
    private void initConfig() {
        //
        // Plugin
        //
        prefix = getPrefix();
        versionInfo = getMessage("plugin.version");
        configReloaded = getMessage("plugin.reload");

        //
        // Error
        //
        notEnoughArguments = getMessage("error.notEnoughArguments");
        notEnoughStoredItem = getMessage("error.notEnoughStoredItems");
        notEnoughMoney = getMessage("error.notEnoughMoney");
        noPlayerFound = getMessage("error.noPlayerFound");
        noItemFound = getMessage("error.noItemFound");
        noParamExist = getMessage("error.noParameterExist");
        invalidArguments = getMessage("error.invalidArguments");
        invalidNumberFormat = getMessage("error.invalidNumber");
        errorOccurred = getMessage("error.errorOccurred");
        errorOccurredOnGUI = getMessage("error.errorOccurredGUI");
        errorFetchCategoryName = getMessage("error.errorFetchCategoryName");
        errorFetchItemConfig = getMessage("error.errorFetchItemConfig");
        // errorFetchDisplayName = getMessage("error.errorFetchDisplayName");
        cannotGiveYourself = getMessage("error.giveMyself");
        permissionDenied = getMessage("error.permissionDenied");
        playerOnly = getMessage("error.playerOnly");
        economyIsNull = getMessage("error.economyIsNull");

        //
        // Command
        //
        commandHelpHeader = getMessage("command.commandHelpHeader");
        adminCommandHelpHeader = getMessage("command.adminCommandHelpHeader");
        disabledWorld = getMessage("command.disabledWorld");
        successGive = getMessage("command.successGive");
        successReceive = getMessage("command.successReceive");
        successGiveAdmin = getMessage("command.successGiveAdmin");
        successSet = getMessage("command.successSet");
        successSell = getMessage("command.successSell");
        successTake = getMessage("command.successTake");

        //
        // Command Description
        //
        boxDesc = getRawMessage("commandDescription.box");
        autoStoreDesc = getRawMessage("commandDescription.autoStore");
        autoStoreListDesc = getRawMessage("commandDescription.autoStoreList");
        giveDesc = getRawMessage("commandDescription.give");
        sellDesc = getRawMessage("commandDescription.sell");
        sellPriceDesc = getRawMessage("commandDescription.sellPrice");
        sellPriceListDesc = getRawMessage("commandDescription.sellPriceList");
        helpDesc = getRawMessage("commandDescription.help");
        versionDesc = getRawMessage("commandDescription.version");

        //
        // Admin Command Description
        //
        addCategoryDesc = getRawMessage("adminCommandDescription.addCategory");
        autoStoreAdminDesc = getRawMessage("adminCommandDescription.autoStore");
        autoStoreListAdminDesc = getRawMessage("adminCommandDescription.autoStoreList");
        giveAdminDesc = getRawMessage("adminCommandDescription.give");
        setDesc = getRawMessage("adminCommandDescription.set");
        takeDesc = getRawMessage("adminCommandDescription.take");
        helpAdminDesc = getRawMessage("adminCommandDescription.help");
        reloadDesc = getRawMessage("adminCommandDescription.reload");

        //
        // Price
        //
        sellPriceListHeader = getMessage("price.sellPriceListHeader");
        sellPriceListFormat = getRawMessage("price.sellPriceFormat");
        sellPriceFormat = getMessage("price.sellPriceFormat");

        //
        // AutoStore
        //
        autoStoreEnabled = getMessage("autoStore.enabled");
        autoStoreDisabled = getMessage("autoStore.disabled");
        autoStoreSettingChanged = getMessage("autoStore.settingChanged");
        autoStoreSettingChangedAll = getMessage("autoStore.settingChangedAll");
        autoStoreListHeader = getMessage("autoStore.listHeader");
        autoStoreListFormat = getRawMessage("autoStore.listFormat");

        //
        // Database
        //
        databaseConnectionReset = getMessage("database.connectionReset");
        databaseInvalidValue = getMessage("database.invalidValue");
        databaseNoColumn = getMessage("database.noColumn");
        databasePlayerAdded = getMessage("database.playerAdded");
        databasePlayerRemoved = getMessage("database.playerRemoved");

        //
        // Map
        //
        mapPlayersRecord = getMessage("map.playersRecord");
        mapColumnsList = getMessage("map.columnsList");
        mapFormat = getMessage("map.format");
    }

    /**
     * 設定ファイルからメッセージを取得し、それにBoxのprefixをつけて返す。
     *
     * @param key YAML キー
     * @return メッセージ
     * @author akaregi
     * @since v1.1.0
     */
    @NotNull
    private String getMessage(@NotNull String key) {
        return prefix + getRawMessage(key);
    }

    /**
     * 設定ファイルからメッセージを取得する。
     *
     * @param key YAML キー
     * @return 生メッセージ
     * @author LazyGon
     * @since v1.1.0
     */
    @NotNull
    private String getRawMessage(@NotNull String key) {
        return convertColor(Optional.ofNullable(config.getString(key)).orElse(getDefaultMessage(key)));
    }

    /**
     * jarの中にあるmessages.ymlからメッセージを取得する。
     *
     * @param key YAML キー
     * @return メッセージ
     * @author LazyGon
     * @since v1.1.0
     */
    private String getDefaultMessage(@NotNull String key) {
        InputStream messageConfigStream = Optional.ofNullable(Box.getInstance().getResource("messages.yml"))
                .orElseThrow(() -> new NoSuchElementException("No message file."));

        YamlConfiguration jarConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(messageConfigStream));

        return Optional.ofNullable(jarConfig.getString(key))
                .orElseThrow(() -> new NoSuchElementException("No such YAML key: " + key));
    }

    /**
     * 設定ファイルからプラグインのプレフィックスを取得する。
     *
     * @return prefix
     * @author LazyGon
     * @since v1.1.0
     */
    @NotNull
    private String getPrefix() {
        String key = "plugin.prefix";
        return convertColor(Optional.ofNullable(config.getString(key)).orElse(getDefaultMessage(key)));
    }

    /**
     * {@link ChatColor#translateAlternateColorCodes(char, String)}
     *
     * @param target
     * @return replaced target
     */
    @NotNull
    public static String convertColor(@NotNull String target) {
        return ChatColor.translateAlternateColorCodes('&', target);
    }
}
