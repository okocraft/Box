package net.okocraft.box.core;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.event4j.bus.EventBus;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.event.feature.FeatureEvent;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.model.data.CustomDataContainer;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.taskfactory.TaskFactory;
import net.okocraft.box.api.util.ExecutorProvider;
import net.okocraft.box.core.command.BoxAdminCommandImpl;
import net.okocraft.box.core.command.BoxCommandImpl;
import net.okocraft.box.core.config.Settings;
import net.okocraft.box.core.listener.DebugListener;
import net.okocraft.box.core.listener.PlayerConnectionListener;
import net.okocraft.box.core.listener.StockHolderListener;
import net.okocraft.box.core.message.ErrorMessages;
import net.okocraft.box.core.message.MicsMessages;
import net.okocraft.box.core.model.data.BoxCustomDataContainer;
import net.okocraft.box.core.model.manager.BoxItemManager;
import net.okocraft.box.core.model.manager.BoxStockManager;
import net.okocraft.box.core.model.manager.BoxUserManager;
import net.okocraft.box.core.player.BoxPlayerMapImpl;
import net.okocraft.box.core.storage.Storage;
import net.okocraft.box.core.storage.implementations.yaml.YamlStorage;
import net.okocraft.box.core.task.AutoSaveTask;
import net.okocraft.box.core.taskfactory.BoxTaskFactory;
import net.okocraft.box.core.util.executor.BoxExecutorProvider;
import net.okocraft.box.core.util.executor.InternalExecutors;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BoxPlugin implements BoxAPI {

    private final JavaPlugin plugin;
    private final Path pluginDirectory;
    private final Path jarFile;

    private final YamlConfiguration configuration;
    private final TranslationDirectory translationDirectory;
    private final DebugListener debugListener = new DebugListener();

    private final EventBus eventBus = EventBus.newEventBus();
    private final BoxTaskFactory taskFactory = new BoxTaskFactory();
    private final BoxExecutorProvider executorProvider = new BoxExecutorProvider();

    private final BoxCommandImpl boxCommand = new BoxCommandImpl();
    private final BoxAdminCommandImpl boxAdminCommand = new BoxAdminCommandImpl();

    private final List<BoxFeature> features = new ArrayList<>();

    private final AutoSaveTask autoSaveTask = new AutoSaveTask();
    private final StockHolderListener stockHolderListener = new StockHolderListener();

    private Storage storage;
    private BoxItemManager itemManager;
    private BoxStockManager stockManager;
    private BoxUserManager userManager;
    private BoxCustomDataContainer customDataContainer;
    private BoxPlayerMapImpl playerMap;

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

        if (configuration.get(Settings.DEBUG)) {
            debugListener.register();
            getLogger().info("Debug mode is ENABLED");
        }

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

        customDataContainer = new BoxCustomDataContainer(storage.getCustomDataStorage());

        playerMap = new BoxPlayerMapImpl(userManager, stockManager);
        playerMap.loadAll();

        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(playerMap), plugin);

        autoSaveTask.start();
        stockHolderListener.register();

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

        getLogger().info("Registering async-tab-completion listener...");

        Bukkit.getPluginManager().registerEvents(boxCommand, plugin);
        Bukkit.getPluginManager().registerEvents(boxAdminCommand, plugin);

        getLogger().info("Successfully enabled!");

        return true;
    }

    public void disable() {
        getLogger().info("Unregistering all listeners...");
        HandlerList.unregisterAll(getPluginInstance());

        getLogger().info("Disabling features...");
        List.copyOf(features).forEach(this::unregister);

        stockHolderListener.unregister();
        autoSaveTask.stop();

        if (playerMap != null) {
            playerMap.unloadAll();
        }

        debugListener.unregister();

        getLogger().info("Closing the storage...");
        try {
            storage.close();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not close the storage", e);
        }

        getLogger().info("Shutting down executors...");

        try {
            taskFactory.shutdown();
            InternalExecutors.shutdownAll();
        } catch (InterruptedException e) {
            getLogger().log(Level.SEVERE, "Could not shutdown executors", e);
        }

        getLogger().info("Unloading translations...");
        translationDirectory.unload();

        getLogger().info("Successfully disabled!");
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        var playerMessenger = new Consumer<Supplier<Component>>() {
            @Override
            public void accept(Supplier<Component> componentSupplier) {
                if (sender instanceof Player) {
                    sender.sendMessage(componentSupplier.get());
                }
            }
        };

        debugListener.unregister();

        if (!(sender instanceof ConsoleCommandSender)) {
            getLogger().info("Reloading box...");
        }

        try {
            configuration.reload();
            sender.sendMessage(MicsMessages.CONFIG_RELOADED);
        } catch (Throwable e) {
            playerMessenger.accept(() -> ErrorMessages.ERROR_RELOAD_FAILURE.apply("config.yml", e));
            getLogger().log(Level.SEVERE, "Could not reload config.yml", e);
        }

        if (configuration.get(Settings.DEBUG)) {
            debugListener.register();
            getLogger().info("Debug mode is ENABLED");
        }

        try {
            translationDirectory.createDirectoryIfNotExists(this::saveDefaultLanguages);
            translationDirectory.load();
            sender.sendMessage(MicsMessages.LANGUAGES_RELOADED);
        } catch (Throwable e) {
            playerMessenger.accept(() -> ErrorMessages.ERROR_RELOAD_FAILURE.apply("languages", e));
            getLogger().log(Level.SEVERE, "Could not reload languages", e);
        }

        for (var feature : features) {
            if (feature instanceof Reloadable reloadable) {
                try {
                    reloadable.reload(sender);
                    eventBus.callEvent(new FeatureEvent(feature, FeatureEvent.Type.RELOAD));
                } catch (Throwable e) {
                    playerMessenger.accept(() -> ErrorMessages.ERROR_RELOAD_FAILURE.apply(feature.getName(), e));
                    getLogger().log(Level.SEVERE, "Could not reload " + feature.getName(), e);
                }
            }
        }

        if (!(sender instanceof ConsoleCommandSender)) {
            getLogger().info("Successfully reloaded!");
        }
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
    public @NotNull CustomDataContainer getCustomDataContainer() {
        return customDataContainer;
    }

    @Override
    public @NotNull TaskFactory getTaskFactory() {
        return taskFactory;
    }

    @Override
    public @NotNull ExecutorProvider getExecutorProvider() {
        return executorProvider;
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
    public @NotNull @Unmodifiable List<BoxFeature> getFeatures() {
        return List.copyOf(features);
    }

    @Override
    public void register(@NotNull BoxFeature boxFeature) {
        if (configuration.get(Settings.DISABLED_FEATURES).contains(boxFeature.getName())) {
            getLogger().warning("Feature " + boxFeature.getName() + " is disabled by disabled-features in config.yml");
            return;
        }

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

        eventBus.callEvent(new FeatureEvent(boxFeature, FeatureEvent.Type.REGISTER));

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

        eventBus.callEvent(new FeatureEvent(boxFeature, FeatureEvent.Type.UNREGISTER));
    }

    @Override
    public boolean isDisabledWorld(@NotNull Player player) {
        return configuration.get(Settings.DISABLED_WORLDS).contains(player.getWorld().getName()) &&
                !player.hasPermission("box.admin.ignore-disabled-world");
    }

    @Override
    public @NotNull NamespacedKey createNamespacedKey(@NotNull String value) {
        return new NamespacedKey(plugin, value);
    }
}
