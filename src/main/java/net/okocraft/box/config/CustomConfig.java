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
public class CustomConfig {

    private static final Box plugin = Box.getInstance();
    private final File file;
    private final String name;
    private FileConfiguration config;

    CustomConfig(String name) {
        this.name = name;
        this.file = new File(plugin.getDataFolder(), this.name);
        initConfig();
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
        initConfig();
    }

    /**
     * Gets FileConfiguration of {@code file}.
     *
     * @return FileConfiguration
     * @author LazyGon
     */
    FileConfiguration getConfig() {
        if (config == null) {
            initConfig();
        }

        return config;
    }

    /**
     * Loads FileConfiguration from {@code file}.
     *
     * @author LazyGon
     */
    void initConfig() {
        saveDefaultConfig();
        config = YamlConfiguration.loadConfiguration(file);
        Optional<InputStream> inputStream = Optional.ofNullable(plugin.getResource(name));
        inputStream.ifPresent(stream -> config.setDefaults(YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8)
        )));
    }

    /**
     * Saves default file which is included in jar.
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