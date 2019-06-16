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

package net.okocraft.box.util;

import java.util.Optional;
import javax.annotation.Nonnull;

import lombok.Getter;

import org.bukkit.configuration.file.FileConfiguration;

import net.okocraft.box.Box;

public class MessageConfig {
    private CustomConfig configFile;
    private FileConfiguration config;

    // Prefix
    @Getter
    private String prefix;

    // notEnough*
    @Getter
    private String notEnoughArguments;
    @Getter
    private String notEnoughStoredItem;

    // no*
    @Getter
    private String noPlayerFound;
    @Getter
    private String noItemFound;
    @Getter
    private String noParamExist;

    // Invalid
    @Getter
    private String invalidArguments;
    @Getter
    private String invalidNumberFormat;
    @Getter
    private String invalidDatabaseNumberFormat;
    @Getter
    private String invalidValueStored;

    // Set, Give, Take
    @Getter
    private String successfullySet;
    @Getter
    private String successfullyGive;
    @Getter
    private String successfullyGiveAdmin;
    @Getter
    private String successfullyTake;

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
    private String autoStorePageIndexChanged;
    @Getter
    private String autoStoreListHeader;
    @Getter
    private String autoStoreListFormat;

    // Database
    @Getter
    private String databaseSetValueSuccess;
    @Getter
    private String databaseNoColumnFound;
    @Getter
    private String databaseRemovePlayerSuccess;
    @Getter
    private String databaseAddPlayerSuccess;
    @Getter
    private String databaseConnectionReset;

    // Map
    @Getter
    private String mapPlayersRecord;
    @Getter
    private String mapColumnsList;
    @Getter
    private String mapFormat;

    // error
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
    private String errorFetchDisplayName;

    // plugin
    @Getter
    private String configReloaded;
    @Getter
    private String versionInfo;

    public MessageConfig(Box plugin) {
        configFile = new CustomConfig(plugin, "messages.yml");
        configFile.saveDefaultConfig();

        config = configFile.getConfig();

        initConfig();
    }

    public void reload() {
        configFile.initConfig();
        config = configFile.getConfig();

        initConfig();
    }

    private void initConfig() {
        //
        // Prefix
        //
        prefix = "§8[§6Box§8]§7 ";

        //
        // notEnough*
        //
        notEnoughArguments = getMessage("NoEnoughArgument", "§c引数が足りません。");
        notEnoughStoredItem = getMessage("NoEnoughStore", "§c在庫が足りません");

        //
        // no*
        //
        noPlayerFound = getMessage("NoPlayerFound", "§cその名前のプレイヤーは登録されていません");
        noItemFound = getMessage("NoItemFound", "§cその名前のアイテムは登録されていません");
        noParamExist = getMessage("NoParamExist", "§c指定された引数は存在しません");

        //
        // Invalid
        //
        invalidArguments = getMessage("InvalidArgument","§c引数が不正です");
        invalidNumberFormat = getMessage("InvalidNumberFormat", "§c数字のフォーマットが不正です");

        invalidDatabaseNumberFormat = getMessage(
                "DatabaseInvalidNumberFormat",
                "&c不正な数字が記録されています。管理者に報告して下さい。"
        );
        invalidValueStored = getMessage(
                "InvalidValueIsStored",
                "&cデータベースに不正な値が格納されています。管理者に報告して下さい。"
        );

        //
        // Give, Set, Take
        //
        successfullyGive = getMessage(
                "SuccessfullyGive",
                "&e%player%のボックスの%item%を%amount%増やしました。(現在%newamount%)"
        );
        successfullyGiveAdmin = getMessage(
                "SuccessfullyGiveNonAdmin",
                "&e%player%に%item%を%amount%個渡しました。(現在%newamount%)"
        );
        successfullySet = getMessage(
                "SuccessfullySet",
                "&e%player%のボックスの%item%を%amount%にセットしました。"
        );
        successfullyTake = getMessage(
                "SuccessfullyTake",
                "&e%player%のボックスの%item%を%amount%減らしました。(現在%newamount%)"
        );

        //
        // AutoStore
        //
        autoStoreEnabled = getMessage(
                "AutoStoreEnabled",
                "&a自動収納が有効化されました。"
        );
        autoStoreDisabled = getMessage(
                "AutoStoreDisabled",
                "&c自動収納が無効化されました。"
        );
        autoStoreSettingChanged = getMessage(
                "AutoStoreSettingChanged",
                "&7%item%のAutoStore設定を&b%isEnabled%&7に設定しました。"
        );
        autoStoreSettingChangedAll = getMessage(
                "AllAutoStoreSettingChanged",
                "&7全てのアイテムのAutoStore設定を&b%isEnabled%&7に設定しました。"
        );
        autoStorePageIndexChanged = getMessage(
                "AutoStoreValueChangeInPage",
                "&b%page%&7ページ目の全ての自動収納の値を&b%newValue%&7に変更しました。"
        );
        autoStoreListHeader = getMessage(
                "AutoStoreListHeader",
                "&7=====&6自動回収設定一覧 %page%ページ目 &a%currentline% &7/ &a%maxline% &7(&b%player%&7)====="
        );
        autoStoreListFormat = getMessage(
                "AutoStoreListFormat",
                "&a%item%&7: &b%isEnabled%"
        );

        //
        // Database
        //
        databaseSetValueSuccess = getMessage(
                "DatabaseSetValueSuccess",
                "&b%uuid% &7(%player%)の %column% を %value% にセットしました。"
        );
        databaseNoColumnFound = getMessage(
                "DatabaseNoColumnFound",
                "&cその名前の列はありません。"
        );
        databaseAddPlayerSuccess = getMessage(
                "DatabaseRemovePlayerSuccess",
                "&7データベースに&b%uuid% &7- &b%player%&7を追加しました。"
        );
        databaseRemovePlayerSuccess = getMessage(
                "DatabaseAddPlayerSuccess",
                "&7データベースから&b%uuid% &7- &b%player%&7を削除しました。"
        );
        databaseConnectionReset = getMessage(
                "DatabaseConnectionReset",
                "§eデータベースへの接続をリセットしました。"
        );

        //
        // Map
        //
        mapPlayersRecord = getMessage(
                "MapPlayersRecord",
                "記録されているプレイヤー"
        );
        mapColumnsList = getMessage(
                "MapColumnsList",
                "列リスト"
        );
        mapFormat = getMessage(
                "MapFormat",
                "%s - %s"
        );

        //
        // Other
        //
        cannotGiveYourself = getMessage(
                "CannotGiveYourself",
                "&c自分自身にアイテムを渡すことはできません。"
        );
        errorOccurred = getMessage(
                "ErrorOccured",
                "&cエラーが発生して処理を実行できませんでした。"
        );
        errorOccurredOnGUI = getMessage(
                "ErrorOnOpenGui",
                "&cエラーが発生してGuiを開けませんでした。"
        );
        errorFetchCategoryName = getMessage(
                "ErrorFetchCategoryName",
                "&cカテゴリ名の取得に失敗しました。"
        );
        errorFetchItemConfig = getMessage(
                "ErrorFetchItemConfig",
                "&cアイテム設定の取得に失敗しました。"
        );
        errorFetchDisplayName = getMessage(
                "ErrorFetchDisplayName",
                "&c表示名が定義されていません。"
        );

        //
        // Config
        //
        versionInfo = getMessage(
                "VersionInfo",
                "&7バージョン %version%"
        );
        configReloaded = getMessage(
                "ConfigReloaded",
                "&7設定を再読込しました。"
        );
    }

    /**
     * Gets message from config file.
     *
     * @param required Message key to get
     * @param def      Alternative message.
     *
     * @return Message. If required's key is not exist, return def.
     */
    @Nonnull
    private String getMessage(@Nonnull String required, @Nonnull String def) {
        return prefix + Optional.ofNullable(config.getString(required))
                .map(MessageUtil::convertColorCode)
                .orElse(MessageUtil.convertColorCode(def));
    }
}
