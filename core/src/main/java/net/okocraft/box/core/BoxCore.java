package net.okocraft.box.core;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import com.github.siroshun09.event4j.bus.EventBus;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.feature.FeatureEvent;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.model.data.CustomDataContainer;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.core.command.BoxAdminCommandImpl;
import net.okocraft.box.core.command.BoxCommandImpl;
import net.okocraft.box.core.config.Settings;
import net.okocraft.box.core.listener.DebugListener;
import net.okocraft.box.core.listener.PlayerConnectionListener;
import net.okocraft.box.core.message.ErrorMessages;
import net.okocraft.box.core.message.MicsMessages;
import net.okocraft.box.core.model.data.BoxCustomDataContainer;
import net.okocraft.box.core.model.loader.ItemLoader;
import net.okocraft.box.core.model.manager.item.BoxItemManager;
import net.okocraft.box.core.model.manager.stock.BoxStockManager;
import net.okocraft.box.core.model.manager.user.BoxUserManager;
import net.okocraft.box.core.player.BoxPlayerMapImpl;
import net.okocraft.box.core.scheduler.FoliaSchedulerWrapper;
import net.okocraft.box.storage.api.holder.StorageHolder;
import net.okocraft.box.storage.api.model.Storage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BoxCore implements BoxAPI {

    private final PluginContext context;

    private final YamlConfiguration configuration;
    private final TranslationDirectory translationDirectory;

    private final FoliaSchedulerWrapper scheduler = new FoliaSchedulerWrapper();

    private final BoxCommandImpl boxCommand = new BoxCommandImpl();
    private final BoxAdminCommandImpl boxAdminCommand = new BoxAdminCommandImpl();

    private final List<BoxFeature> features = new ArrayList<>();

    private Storage storage;
    private BoxItemManager itemManager;
    private BoxStockManager stockManager;
    private BoxUserManager userManager;
    private BoxCustomDataContainer customDataContainer;
    private BoxPlayerMapImpl playerMap;

    public BoxCore(@NotNull PluginContext context) {
        this.context = context;

        this.configuration = context.configuration();
        this.translationDirectory = context.translationDirectory();

        BoxProvider.set(this);
    }

    public boolean load() {
        getLogger().info("Loading config.yml...");

        try {
            ResourceUtils.copyFromJarIfNotExists(context.jarFile(), "config.yml", configuration.getPath());
            configuration.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load config.yml", e);
            return false;
        }

        getLogger().info("Loading languages...");

        try {
            translationDirectory.load();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not load languages", e);
            return false;
        }

        if (configuration.get(Settings.DEBUG)) {
            DebugListener.register(getEventBus());
            getLogger().info("Debug mode is ENABLED");
        }

        getLogger().info("Successfully loaded!");

        return true;
    }

    public boolean enable() {
        var storageSection = configuration.getOrCreateSection("storage");

        var storageType = storageSection.getString("type");
        var storageFunction = context.storageRegistry().getStorageFunction(storageType);

        if (storageFunction == null) {
            if (!storageType.isEmpty()) {
                getLogger().warning(storageType + " is not found!");
                getLogger().warning("Using " + context.storageRegistry().getDefaultStorageName() + " storage...");
            }

            storage = context.storageRegistry().createDefaultStorage(storageSection);
        } else {
            storage = storageFunction.apply(storageSection);
        }

        getLogger().info("Initializing " + storage.getName() + " storage...");

        try {
            storage.init();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not initialize " + storage.getName() + " storage", e);
            return false;
        }

        StorageHolder.init(storage); // set storage for other features/plugins that depend on Storage API

        getLogger().info("Initializing managers...");

        userManager = new BoxUserManager(storage.getUserStorage());

        try {
            itemManager = ItemLoader.load(storage.getItemStorage());
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not import items", e);
            return false;
        }

        stockManager = new BoxStockManager(storage.getStockStorage(), uuid -> Bukkit.getPlayer(uuid) != null);

        customDataContainer = new BoxCustomDataContainer(storage.getCustomDataStorage(), context.executorProvider().newSingleThreadExecutor("Custom Data"));

        playerMap = new BoxPlayerMapImpl(userManager, stockManager, context.executorProvider().newSingleThreadScheduler("Player Loader"));
        playerMap.loadAll();

        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(playerMap), context.plugin());

        stockManager.schedulerAutoSaveTask(scheduler);

        getLogger().info("Registering commands...");

        Optional.ofNullable(context.plugin().getCommand("box"))
                .ifPresentOrElse(
                        boxCommand::register,
                        () -> {
                            throw new IllegalStateException("Could not get command /box");
                        }
                );

        Optional.ofNullable(context.plugin().getCommand("boxadmin"))
                .ifPresentOrElse(
                        boxAdminCommand::register,
                        () -> {
                            throw new IllegalStateException("Could not get command /boxadmin");
                        }
                );

        getLogger().info("Registering async-tab-completion listener...");

        Bukkit.getPluginManager().registerEvents(boxCommand, context.plugin());
        Bukkit.getPluginManager().registerEvents(boxAdminCommand, context.plugin());

        return true;
    }

    public void disable() {
        getLogger().info("Unregistering all listeners...");
        HandlerList.unregisterAll(getPluginInstance());

        if (!features.isEmpty()) {
            getLogger().info("Disabling features...");
            List.copyOf(features).forEach(this::unregister);
        }

        BoxProvider.unset();

        if (playerMap != null) {
            playerMap.unloadAll();
        }

        stockManager.close();

        DebugListener.unregister(getEventBus());

        context.eventBus().close();

        getLogger().info("Shutting down executors...");

        context.executorProvider().close(context.plugin());

        getLogger().info("Closing the storage...");

        try {
            storage.close();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not close the storage", e);
        }

        getLogger().info("Unloading translations...");
        translationDirectory.unload();
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

        DebugListener.unregister(getEventBus());

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
            DebugListener.register(getEventBus());
            getLogger().info("Debug mode is ENABLED");
        }

        try {
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
                    getEventBus().callEvent(new FeatureEvent(feature, FeatureEvent.Type.RELOAD));
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

    @Override
    public @NotNull Plugin getPluginInstance() {
        return context.plugin();
    }

    @Override
    public @NotNull Path getPluginDirectory() {
        return context.dataDirectory();
    }

    @Override
    public @NotNull Path getJar() {
        return context.jarFile();
    }

    @Override
    public @NotNull Logger getLogger() {
        return context.plugin().getLogger();
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
    public @NotNull BoxScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public @NotNull BoxPlayerMap getBoxPlayerMap() {
        return playerMap;
    }

    @Override
    public @NotNull EventBus<BoxEvent> getEventBus() {
        return context.eventBus();
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
    public @NotNull
    @Unmodifiable List<BoxFeature> getFeatures() {
        return List.copyOf(features);
    }

    @Override
    public @NotNull <T extends BoxFeature> Optional<T> getFeature(@NotNull Class<T> clazz) {
        return features.stream()
                .filter(feature -> feature.getClass() == clazz)
                .map(clazz::cast)
                .findFirst();
    }

    @Override
    public void register(@NotNull BoxFeature boxFeature) {
        if (boxFeature instanceof Disableable &&
                configuration.get(Settings.DISABLED_FEATURES).contains(boxFeature.getName())) {
            getLogger().warning("The " + boxFeature.getName() + " feature is disabled in config.yml");
            return;
        }

        var dependencies = boxFeature.getDependencies();

        if (!dependencies.isEmpty()) {
            for (var dependencyClass : dependencies) {
                if (features.stream().noneMatch(feature -> dependencyClass.isAssignableFrom(feature.getClass()))) {
                    getLogger().warning(
                            dependencyClass.getSimpleName() + " that is the dependency of the " + boxFeature.getName() + " is not registered."
                    );
                    return;
                }
            }
        }

        try {
            boxFeature.enable();
        } catch (Throwable throwable) {
            getLogger().log(
                    Level.SEVERE,
                    "Could not enable the " + boxFeature.getName() + " feature",
                    throwable
            );
            boxFeature.disable();
            return;
        }

        features.add(boxFeature);

        getEventBus().callEvent(new FeatureEvent(boxFeature, FeatureEvent.Type.REGISTER));

        getLogger().info("The " + boxFeature.getName() + " feature has been enabled.");
    }

    @Override
    public void unregister(@NotNull BoxFeature boxFeature) {
        getLogger().info("Disabling the " + boxFeature.getName() + " feature...");
        features.remove(boxFeature);

        try {
            boxFeature.disable();
        } catch (Throwable throwable) {
            getLogger().log(
                    Level.SEVERE,
                    "Could not disable the " + boxFeature.getName() + " feature",
                    throwable
            );
        }

        context.eventBus().callEvent(new FeatureEvent(boxFeature, FeatureEvent.Type.UNREGISTER));
    }

    @Override
    public boolean isDisabledWorld(@NotNull Player player) {
        return isDisabledWorld(player.getWorld()) && !player.hasPermission("box.admin.ignore-disabled-world");
    }

    @Override
    public boolean isDisabledWorld(@NotNull World world) {
        return isDisabledWorld(world.getName());
    }

    @Override
    public boolean isDisabledWorld(@NotNull String worldName) {
        return configuration.get(Settings.DISABLED_WORLDS).contains(worldName);
    }

    @Override
    public @NotNull NamespacedKey createNamespacedKey(@NotNull String value) {
        return new NamespacedKey(context.plugin(), value);
    }
}
