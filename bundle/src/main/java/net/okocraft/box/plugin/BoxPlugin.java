package net.okocraft.box.plugin;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.okocraft.box.api.model.stock.AbstractStockHolder;
import net.okocraft.box.bootstrap.BootstrapContext;
import net.okocraft.box.core.config.Settings;
import net.okocraft.box.core.event.EventBusHolder;
import net.okocraft.box.core.listener.DebugListener;
import net.okocraft.box.core.util.executor.ExecutorProvider;
import net.okocraft.box.storage.api.registry.StorageRegistry;
import net.okocraft.box.util.TranslationDirectoryUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

public class BoxPlugin extends JavaPlugin {

    private final Path dataDirectory;
    private final Path jarFile;
    private final String version;
    private final StorageRegistry storageRegistry;
    private final ExecutorProvider executorProvider;
    private final YamlConfiguration configuration;
    private final TranslationDirectory translationDirectory;
    private final EventBusHolder eventBusHolder;

    private boolean isLoaded = false;

    public BoxPlugin(@NotNull BootstrapContext context) {
        this.dataDirectory = context.getPluginDirectory();
        this.jarFile = context.getJarFile();
        this.version = context.getVersion();
        this.storageRegistry = context.getStorageRegistry();
        this.executorProvider = context.getExecutorProvider();
        this.configuration = YamlConfiguration.create(context.getPluginDirectory().resolve("config.yml"));
        this.translationDirectory = TranslationDirectoryUtil.fromContext(context);
        this.eventBusHolder = context.getEventBusHolder();
    }

    @Override
    public void onLoad() {
        getLogger().info("Loading config.yml...");

        try {
            ResourceUtils.copyFromJarIfNotExists(jarFile, "config.yml", configuration.getPath());
            configuration.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml", e);
            return;
        }

        getLogger().info("Loading languages...");

        try {
            translationDirectory.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load languages", e);
            return;
        }

        if (configuration.get(Settings.DEBUG)) {
            DebugListener.register(eventBusHolder.getEventBus());
            getLogger().info("Debug mode is ENABLED");
        }

        if (configuration.get(Settings.ALLOW_MINUS_STOCK)) {
            AbstractStockHolder.allowMinus = true;
            getLogger().info("Negative numbers are allowed in the stock");
        }

        isLoaded = true;
        getLogger().info("Successfully loaded!");
    }

    @Override
    public void onEnable() {
        if (!isLoaded) {
            disablePlugin();
        }
    }

    @Override
    public void onDisable() {
    }

    private void disablePlugin() {
        getServer().getPluginManager().disablePlugin(this);
    }
}
