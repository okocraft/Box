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

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class CustomConfig {
    private FileConfiguration config;

    private final File configFile;

    private final String file;

    private final Plugin plugin;

    CustomConfig(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.file = fileName;

        configFile = new File(plugin.getDataFolder(), file);
    }

    /**
     * 設定を取得する。
     *
     * @return FileConfiguration
     */
    FileConfiguration getConfig() {
        if (config == null) {
            initConfig();
        }

        return config;
    }

    /**
     * 設定を読み込む。
     */
    void initConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);

        Optional.ofNullable(plugin.getResource(file)).ifPresent(configStream ->
                config.setDefaults(
                    YamlConfiguration.loadConfiguration(new InputStreamReader(configStream, StandardCharsets.UTF_8))
                )
        );
    }

    /**
     * 設定ファイルを保存する。
     */
    void saveDefaultConfig() {
        if (!configFile.exists()) {
            plugin.saveResource(file, false);
        }
    }
}
