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

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;

/**
 * リソース（YAML 設定）を取り扱うクラス。
 *
 * @author LazyGon
 */
public class CustomConfig {
    /**
     * プラグイン。
     */
    private final Plugin plugin;

    /**
     * 設定の実体ファイル。
     */
    private final File file;

    /**
     * 設定の実体ファイル名。
     */
    private final String name;

    /**
     * 設定。
     */
    private FileConfiguration config;

    CustomConfig(@Nonnull Plugin plugin, @Nonnull String name) {
        this.plugin = plugin;
        this.name = name;

        file = new File(plugin.getDataFolder(), this.name);
    }

    /**
     * 設定を取得する。
     *
     * @return FileConfiguration
     * @author LazyGon
     */
    @Nonnull
    FileConfiguration getConfig() {
        if (config == null) {
            initConfig();
        }

        return config;
    }

    /**
     * 設定を読み込む。
     *
     * @author LazyGon
     */
    void initConfig() {
        config = YamlConfiguration.loadConfiguration(file);
        Optional<InputStream> inputStream = Optional.ofNullable(plugin.getResource(name));
        inputStream.ifPresent(stream -> config.setDefaults(YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        )));
    }

    /**
     * デフォルトの設定ファイルを保存する。
     *
     * @author LazyGon
     */
    void saveDefaultConfig() {
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
    }

    /**
     * 設定ファイルを保存する。
     *
     * @author LazyGon
     */
    public void saveConfig() {
        if (config == null)
            return;
        try {
            getConfig().save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + file, e);
        }
    }
}
