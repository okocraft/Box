package net.okocraft.box.core;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.event4j.bus.EventBus;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.util.Debugger;
import net.okocraft.box.core.command.BoxAdminCommandImpl;
import net.okocraft.box.core.command.BoxCommandImpl;
import net.okocraft.box.core.config.Settings;
import net.okocraft.box.core.listener.PlayerConnectionListener;
import net.okocraft.box.core.model.manager.BoxItemManager;
import net.okocraft.box.core.model.manager.BoxStockManager;
import net.okocraft.box.core.model.manager.BoxUserManager;
import net.okocraft.box.core.player.BoxPlayerMapImpl;
import net.okocraft.box.core.storage.Storage;
import net.okocraft.box.core.storage.implementations.yaml.YamlStorage;
import net.okocraft.box.core.task.ModifiedStockHolderSaveTask;
import net.okocraft.box.core.util.ExecutorProvider;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BoxPlugin implements BoxAPI {

    private final JavaPlugin plugin;
    private final Path pluginDirectory;
    private final Path jarFile;

    private final YamlConfiguration configuration;
    private final TranslationDirectory translationDirectory;

    private final EventBus eventBus = EventBus.newEventBus();
    private final BoxCommandImpl boxCommand = new BoxCommandImpl();
    private final BoxAdminCommandImpl boxAdminCommand = new BoxAdminCommandImpl();

    private final List<BoxFeature> features = new ArrayList<>();

    private Storage storage;
    private BoxItemManager itemManager;
    private BoxStockManager stockManager;
    private BoxUserManager userManager;
    private BoxPlayerMapImpl playerMap;

    private ModifiedStockHolderSaveTask autoSaveTask;

    public BoxPlugin(@NotNull JavaPlugin plugin, @NotNull Path jarFile) {
        this.plugin = plugin;
        this.pluginDirectory = plugin.getDataFolder().toPath();
        this.jarFile = jarFile;

        this.configuration =
                YamlConfiguration.create(pluginDirectory.resolve("config.yml"));
        this.translationDirectory =
                TranslationDirectory.create(pluginDirectory.resolve("languages"), Key.key("box", "language"));

        BoxProvider.set(this);
    }

    public boolean load() {
        getLogger().info("Loading config.yml...");

        try {
            ResourceUtils.copyFromJarIfNotExists(jarFile, "config.yml", configuration.getPath());
            configuration.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml", e);
            return false;
        }

        getLogger().info("Loading languages...");

        translationDirectory.getRegistry().defaultLocale(Locale.JAPAN);

        try {
            translationDirectory.createDirectoryIfNotExists(this::saveDefaultLanguages);
            translationDirectory.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load languages", e);
            return false;
        }

        Debugger.ENABLED = configuration.get(Settings.DEBUG);
        Debugger.log(() -> "Debug mode is ENABLED");

        getLogger().info("Successfully loaded!");

        return true;
    }

    public boolean enable() {
        storage = new YamlStorage(getPluginDirectory().resolve("data")); // TODO: SQLite, MySQL, or something else...

        try {
            storage.init();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not initialize a storage", e);
            return false;
        }

        getLogger().info("Initializing managers...");

        itemManager = new BoxItemManager(storage.getItemStorage());

        try {
            itemManager.importAllItems();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not import items", e);
            return false;
        }

        stockManager = new BoxStockManager(storage.getStockStorage());
        userManager = new BoxUserManager(storage.getUserStorage());

        playerMap = new BoxPlayerMapImpl(userManager, stockManager);
        playerMap.loadAll();

        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(playerMap), plugin);

        autoSaveTask = new ModifiedStockHolderSaveTask(storage);
        autoSaveTask.start();

        getLogger().info("Registering commands...");

        Optional.ofNullable(plugin.getCommand("box"))
                .ifPresentOrElse(
                        boxCommand::register,
                        () -> {
                            throw new IllegalStateException("Could not get command /box");
                        }
                );

        Optional.ofNullable(plugin.getCommand("boxadmin"))
                .ifPresentOrElse(
                        boxAdminCommand::register,
                        () -> {
                            throw new IllegalStateException("Could not get command /boxadmin");
                        }
                );

        getLogger().info("Successfully enabled!");

        return true;
    }

    public void disable() {
        getLogger().info("Unregistering all listeners...");
        HandlerList.unregisterAll(getPluginInstance());

        getLogger().info("Disabling features...");
        List.copyOf(features).forEach(this::unregister);

        if (autoSaveTask != null) {
            autoSaveTask.stop();
        }

        if (playerMap != null) {
            playerMap.unloadAll();
        }

        getLogger().info("Closing the storage...");
        try {
            storage.close();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not close the storage", e);
        }

        getLogger().info("Shutting down executors...");

        try {
            ExecutorProvider.shutdownAll();
        } catch (InterruptedException e) {
            getLogger().log(Level.SEVERE, "Could not shutdown executors", e);
        }

        getLogger().info("Successfully disabled!");
    }

    private void saveDefaultLanguages(@NotNull Path directory) throws IOException {
        var japanese = "ja_JP.yml";
        ResourceUtils.copyFromJar(jarFile, japanese, directory.resolve(japanese));
    }

    @Override
    public @NotNull Plugin getPluginInstance() {
        return plugin;
    }

    @Override
    public @NotNull Path getPluginDirectory() {
        return pluginDirectory;
    }

    @Override
    public @NotNull Path getJar() {
        return jarFile;
    }

    @Override
    public @NotNull Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public @NotNull YamlConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public @NotNull UserManager getUserManager() {
        return userManager;
    }

    @Override
    public @NotNull ItemManager getItemManager() {
        return itemManager;
    }

    @Override
    public @NotNull StockManager getStockManager() {
        return stockManager;
    }

    @Override
    public @NotNull BoxPlayerMap getBoxPlayerMap() {
        return playerMap;
    }

    @Override
    public @NotNull EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public @NotNull BoxCommand getBoxCommand() {
        return boxCommand;
    }

    @Override
    public @NotNull BoxAdminCommand getBoxAdminCommand() {
        return boxAdminCommand;
    }

    @Override
    public void register(@NotNull BoxFeature boxFeature) {
        try {
            boxFeature.enable();
        } catch (Throwable throwable) {
            getLogger().log(
                    Level.SEVERE,
                    "Could not enable the feature: " + boxFeature.getName(),
                    throwable
            );
            boxFeature.disable();
            return;
        }

        features.add(boxFeature);
        getLogger().info("Feature " + boxFeature.getName() + " has been enabled.");
    }

    @Override
    public void unregister(@NotNull BoxFeature boxFeature) {
        getLogger().info("Disabling feature " + boxFeature.getName() + "...");
        features.remove(boxFeature);

        try {
            boxFeature.disable();
        } catch (Throwable throwable) {
            getLogger().log(
                    Level.SEVERE,
                    "Could not disable the feature: " + boxFeature.getName(),
                    throwable
            );
        }
    }

    @Override
    public boolean isDisabledWorld(@NotNull World world) {
        return configuration.get(Settings.DISABLED_WORLDS).contains(world.getName());
    }
}
