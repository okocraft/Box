package net.okocraft.box.core;

import dev.siroshun.event4j.api.listener.ListenerSubscriber;
import dev.siroshun.event4j.api.listener.SubscribedListener;
import dev.siroshun.event4j.api.priority.Priority;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.caller.EventCallerProvider;
import net.okocraft.box.api.event.feature.FeatureEvent;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.feature.FeatureProvider;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.message.MessageProvider;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.core.command.BoxAdminCommandImpl;
import net.okocraft.box.core.command.BoxCommandImpl;
import net.okocraft.box.core.config.Config;
import net.okocraft.box.core.event.BoxEventCallerProvider;
import net.okocraft.box.core.feature.BoxFeatureProvider;
import net.okocraft.box.core.listener.DebugListener;
import net.okocraft.box.core.listener.PlayerConnectionListener;
import net.okocraft.box.core.message.CoreMessages;
import net.okocraft.box.core.model.manager.customdata.BoxCustomDataManager;
import net.okocraft.box.core.model.manager.item.BoxItemManager;
import net.okocraft.box.core.model.manager.stock.BoxStockManager;
import net.okocraft.box.core.model.manager.user.BoxUserManager;
import net.okocraft.box.core.player.BoxPlayerMapImpl;
import net.okocraft.box.storage.api.loader.StorageLoader;
import net.okocraft.box.storage.api.loader.item.ItemLoader;
import net.okocraft.box.storage.api.model.Storage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoxCore implements BoxAPI {

    private final PluginContext context;
    private final EventCallerProvider eventCallers;
    private final ListenerSubscriber<Key, BoxEvent, Priority> listenerSubscriber;

    private SubscribedListener<Key, BoxEvent, Priority> debugListener;

    private BoxItemManager itemManager;
    private BoxStockManager stockManager;
    private BoxUserManager userManager;
    private BoxCustomDataManager customDataManager;
    private BoxPlayerMapImpl playerMap;

    private BoxCommandImpl boxCommand;
    private BoxAdminCommandImpl boxAdminCommand;

    private BoxFeatureProvider boxFeatureProvider;

    public BoxCore(@NotNull PluginContext context) {
        this.context = context;
        this.eventCallers = new BoxEventCallerProvider(context.eventService().caller(), context.scheduler());
        this.listenerSubscriber = context.eventService().subscriber();
    }

    public boolean enable(@NotNull Storage storage) {
        this.toggleDebugListener(this.context.config().coreSetting().debug());

        BoxLogger.logger().info("Initializing the storage...");

        if (!StorageLoader.initialize(storage, this.context.config().getInitialBoxDataJsonFilepath())) {
            return false;
        }

        BoxLogger.logger().info("Initializing managers...");

        this.userManager = new BoxUserManager(storage.getUserStorage());

        try {
            var itemLoadResult = ItemLoader.fromStorage(this.context.defaultItemProvider(), storage);
            var remapItemIds = ItemLoader.loadRemappedItemIds(storage.remappedItemStorage());
            itemLoadResult.logItemCount();
            this.itemManager = new BoxItemManager(storage.customItemStorage(), this.eventCallers, this.context.scheduler(), this.context.defaultItemProvider(), itemLoadResult.asIterator(), remapItemIds);
        } catch (Exception e) {
            BoxLogger.logger().error("Could not load default/custom items", e);
            return false;
        }

        var stockDataSetting = this.context.config().coreSetting().stockData();
        this.stockManager = new BoxStockManager(storage.getStockStorage(), this.eventCallers, this.itemManager::getBoxItemOrNull, stockDataSetting.unloadTime(), stockDataSetting.saveInterval(), TimeUnit.SECONDS);

        this.customDataManager = new BoxCustomDataManager(storage.getCustomDataStorage());

        this.playerMap = new BoxPlayerMapImpl(this.userManager, this.stockManager, this.eventCallers, this.context.scheduler(), this.context.messageProvider());
        this.playerMap.loadAll();

        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this.playerMap), this.context.plugin());

        this.stockManager.schedulerAutoSaveTask(this.context.scheduler());

        BoxLogger.logger().info("Registering commands...");

        this.boxCommand = new BoxCommandImpl(this.context.messageProvider(), this.context.scheduler(), this.playerMap, this::canUseBox);
        this.boxAdminCommand = new BoxAdminCommandImpl(this.context.messageProvider(), this.context.scheduler());

        this.context.commandRegisterer().register(this.boxCommand).register(this.boxAdminCommand);

        Bukkit.getPluginManager().registerEvents(this.boxCommand, this.context.plugin());
        Bukkit.getPluginManager().registerEvents(this.boxAdminCommand, this.context.plugin());

        return true;
    }

    public void disable() {
        if (this.playerMap != null) {
            this.playerMap.unloadAll();
        }

        this.stockManager.close();
        this.toggleDebugListener(false);
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        var source = this.context.messageProvider().findSource(sender);
        var playerMessenger = new Consumer<Supplier<Component>>() {
            @Override
            public void accept(Supplier<Component> componentSupplier) {
                if (sender instanceof Player) {
                    sender.sendMessage(componentSupplier.get());
                }
            }
        };

        if (!(sender instanceof ConsoleCommandSender)) {
            BoxLogger.logger().info("Reloading box...");
        }

        try {
            this.context.config().reload();
            CoreMessages.CONFIG_RELOADED_MSG.apply(Config.FILENAME).source(source).send(sender);
        } catch (Throwable e) {
            playerMessenger.accept(() -> CoreMessages.CONFIG_RELOAD_FAILURE.apply(Config.FILENAME, e).source(source).message());
            BoxLogger.logger().error("Could not reload {}", Config.FILENAME, e);
        }

        this.toggleDebugListener(this.context.config().coreSetting().debug());

        try {
            this.context.messageProvider().load();
            CoreMessages.MESSAGE_RELOADED_MSG.source(source).send(sender);
        } catch (Throwable e) {
            playerMessenger.accept(() -> CoreMessages.MESSAGES_RELOAD_FAILURE.apply(e).source(source).message());
            BoxLogger.logger().error("Could not reload messages", e);
        }

        var featureReloadContext = new FeatureContext.Reloading(this.context.plugin(), sender);

        for (var feature : this.boxFeatureProvider.getFeatures()) {
            if (feature instanceof Reloadable reloadable) {
                try {
                    reloadable.reload(featureReloadContext);
                    this.eventCallers.sync().call(new FeatureEvent(feature, FeatureEvent.Type.RELOAD));
                } catch (Throwable e) {
                    playerMessenger.accept(() -> CoreMessages.FEATURE_RELOAD_FAILURE.apply(feature, e).source(source).message());
                    BoxLogger.logger().error("Could not reload {}", feature.getName(), e);
                }
            }
        }

        if (!(sender instanceof ConsoleCommandSender)) {
            BoxLogger.logger().info("Successfully reloaded!");
        }
    }

    @Override
    public @NotNull Path getPluginDirectory() {
        return this.context.dataDirectory();
    }

    @Override
    public @NotNull MessageProvider getMessageProvider() {
        return this.context.messageProvider();
    }


    @Override
    public @NotNull EventCallerProvider getEventCallers() {
        return this.eventCallers;
    }

    @Override
    public @NotNull ListenerSubscriber<Key, BoxEvent, Priority> getListenerSubscriber() {
        return this.listenerSubscriber;
    }

    @Override
    public @NotNull UserManager getUserManager() {
        return this.userManager;
    }

    @Override
    public @NotNull ItemManager getItemManager() {
        return this.itemManager;
    }

    @Override
    public @NotNull StockManager getStockManager() {
        return this.stockManager;
    }

    @Override
    public @NotNull BoxCustomDataManager getCustomDataManager() {
        return this.customDataManager;
    }

    @Override
    public @NotNull FeatureProvider getFeatureProvider() {
        return this.boxFeatureProvider;
    }

    @Override
    public @NotNull BoxScheduler getScheduler() {
        return this.context.scheduler();
    }

    @Override
    public @NotNull BoxPlayerMap getBoxPlayerMap() {
        return this.playerMap;
    }

    @Override
    public @NotNull BoxCommand getBoxCommand() {
        return this.boxCommand;
    }

    @Override
    public @NotNull BoxAdminCommand getBoxAdminCommand() {
        return this.boxAdminCommand;
    }

    public void initializeFeatures(@NotNull List<BoxFeature> features) {
        if (features.isEmpty()) {
            this.boxFeatureProvider = new BoxFeatureProvider(Collections.emptyMap());
            return;
        }

        var featureMap = new LinkedHashMap<Class<? extends BoxFeature>, BoxFeature>();
        this.boxFeatureProvider = new BoxFeatureProvider(Collections.unmodifiableMap(featureMap));

        var context = new FeatureContext.Enabling(this.context.plugin());

        for (var feature : features) {
            initializeFeature(feature, featureMap, context);
            this.eventCallers.sync().call(new FeatureEvent(feature, FeatureEvent.Type.ENABLE));
        }
    }

    private static void initializeFeature(@NotNull BoxFeature feature, @NotNull Map<Class<? extends BoxFeature>, BoxFeature> registry, @NotNull FeatureContext.Enabling context) {
        if (registry.containsKey(feature.getClass())) {
            throw new IllegalStateException("%s is registered twice.".formatted(feature.getName()));
        }

        var dependencies = feature.getDependencies();

        for (var dependencyClass : dependencies) {
            if (!registry.containsKey(dependencyClass)) {
                throw new IllegalStateException("%s that is the dependency of %s is not registered.".formatted(dependencyClass.getName(), feature.getName()));
            }
        }

        try {
            feature.enable(context);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to enable %s.".formatted(feature.getName()), e);
        }

        registry.put(feature.getClass(), feature);
    }

    public void disableAllFeatures() {
        var features = this.getFeatureProvider().getFeatures();

        if (features.isEmpty()) {
            return;
        }

        var context = new FeatureContext.Disabling(this.context.plugin());

        for (var feature : features) {
            try {
                feature.disable(context);
            } catch (Throwable throwable) {
                BoxLogger.logger().error("Failed to disable {}.", feature.getName(), throwable);
                continue;
            }

            this.eventCallers.sync().call(new FeatureEvent(feature, FeatureEvent.Type.DISABLE));
        }
    }

    @Override
    public boolean canUseBox(@NotNull Player player) {
        return !this.isDisabledWorld(player.getWorld()) || player.hasPermission("box.admin.ignore-disabled-world");
    }

    @Override
    public boolean isDisabledWorld(@NotNull World world) {
        return this.isDisabledWorld(world.getName());
    }

    @Override
    public boolean isDisabledWorld(@NotNull String worldName) {
        return this.context.config().coreSetting().disabledWorlds().contains(worldName);
    }

    private void toggleDebugListener(boolean enabled) {
        if (enabled) {
            if (this.debugListener == null) {
                this.debugListener = DebugListener.register(this.listenerSubscriber);
            }
            BoxLogger.logger().info("Debug mode is ENABLED");
        } else if (this.debugListener != null) {
            this.listenerSubscriber.unsubscribe(this.debugListener);
            BoxLogger.logger().info("Debug mode has been disabled.");
        }
    }
}
