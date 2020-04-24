package net.okocraft.box.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.okocraft.box.Box;

/**
 * Class for manipulating yaml files.
 *
 * @author LazyGon
 */
abstract class CustomConfig {

    private final Box plugin = Box.getInstance();
    private final File file;
    private final String name;
    private FileConfiguration config;

    CustomConfig(String name) {
        this.name = name;
        this.file = new File(plugin.getDataFolder(), this.name);
        reload();
        if (file.isDirectory()) {
            throw new IllegalArgumentException("file must not be directory");
        }
    }

    CustomConfig(File file) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("file must not be directory");
        }
        this.file = file;
        this.name = file.getName();
        reload();
    }

    /**
     * このインスタンスが保持する{@link FileConfiguration}を取得する。
     *
     * @return FileConfiguration
     * @author LazyGon
     */
    protected FileConfiguration get() {
        if (config == null) {
            reload();
        }

        return config;
    }

    /**
     * {@code file}から{@link FileConfiguration}を読み込み、{@code config}に代入する。
     */
    public void reload() {
        saveDefault();
        config = YamlConfiguration.loadConfiguration(file);
        Optional<InputStream> inputStream = Optional.ofNullable(plugin.getResource(name));
        inputStream.ifPresent(stream -> config.setDefaults(YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        )));
    }

    /**
     * JARの中のデフォルトのymlファイルをプラグインフォルダーに保存する。
     */
    protected void saveDefault() {
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
    }

    /**
     * 設定ファイルを保存する。
     *
     * @author LazyGon
     */
    protected void save() {
        if (config == null)
            return;
        try {
            get().save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + file, e);
        }
    }
}