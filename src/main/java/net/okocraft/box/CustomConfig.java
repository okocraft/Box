package net.okocraft.box;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

class CustomConfig {
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
