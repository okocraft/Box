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

import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nonnull;

import lombok.Getter;

import lombok.val;
import org.bukkit.configuration.file.FileConfiguration;

import net.okocraft.box.Box;

/**
 * メッセージ設定クラス
 *
 * @author akaregi
 * @since v1.1.0
 */
public class MessageConfig {
    private CustomConfig configFile;

    /**
     * プラグインのデータフォルダ(plugins/Box/)に存在する messages.yml 。
     */
    private FileConfiguration config;

    // Plugin
    @Getter
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
    // @Getter
    // private String errorFetchDisplayName;

    // Command
    @Getter
    private String successSet;
    @Getter
    private String successTake;
    @Getter
    private String successGive;
    @Getter
    private String successGiveAdmin;

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
    private String autoStoreSettingChangedPage;
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

        //
        // Command
        //
        successGive = getMessage("command.successGive");
        successGiveAdmin = getMessage("command.successGiveAdmin");
        successSet = getMessage("command.successSet");
        successTake = getMessage("command.successTake");

        //
        // AutoStore
        //
        autoStoreEnabled = getMessage("autoStore.enabled");
        autoStoreDisabled = getMessage("autoStore.disabled");
        autoStoreSettingChanged = getMessage("autoStore.settingChanged");
        autoStoreSettingChangedAll = getMessage("autoStore.settingChangedAll");
        autoStoreSettingChangedPage = getMessage("autoStore.settingChangedPage");
        autoStoreListHeader = getMessage("autoStore.listHeader");
        autoStoreListFormat = getMessage("autoStore.listFormat");

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
     * 設定ファイルからメッセージを取得する。
     *
     * @author akaregi
     * @since v1.1.0
     *
     * @param key YAML キー
     *
     * @return メッセージ
     */
    @Nonnull
    private String getMessage(@Nonnull String key) {
        return prefix + Optional.ofNullable(config.getString(key))
                .map(MessageUtil::convertColorCode)
                // FIXME この実装ではアップデートごとにNoSuchElementExceptionを吐いてしまう可能性がある。
                .orElseThrow(() -> new NoSuchElementException("No such YAML key: " + key));
    }

    /**
     * 設定ファイルからプラグインのプレフィックスを取得する。
     * 
     * @author LazyGon
     * @since v1.1.0
     * 
     * @return prefix
     */
    @Nonnull
    private String getPrefix() {
        return Optional.ofNullable(config.getString("plugin.prefix"))
                .map(MessageUtil::convertColorCode)
                .orElseThrow(() -> new NoSuchElementException("No prefix specified"));
    }
}
