package net.okocraft.box.core;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.command.base.BoxCommand;
import net.okocraft.box.api.event.feature.FeatureEvent;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.model.manager.ItemManager;
import net.okocraft.box.api.model.manager.StockManager;
import net.okocraft.box.api.model.manager.UserManager;
import net.okocraft.box.api.player.BoxPlayerMap;
import net.okocraft.box.api.scheduler.BoxScheduler;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.core.command.BoxAdminCommandImpl;
import net.okocraft.box.core.command.BoxCommandImpl;
import net.okocraft.box.core.config.Config;
import net.okocraft.box.core.listener.DebugListener;
import net.okocraft.box.core.listener.PlayerConnectionListener;
import net.okocraft.box.core.message.ErrorMessages;
import net.okocraft.box.core.message.MicsMessages;
import net.okocraft.box.core.model.manager.customdata.BoxCustomDataManager;
import net.okocraft.box.core.model.manager.event.BoxEventManager;
import net.okocraft.box.core.model.manager.item.BoxItemManager;
import net.okocraft.box.core.model.manager.stock.BoxStockManager;
import net.okocraft.box.core.model.manager.user.BoxUserManager;
import net.okocraft.box.core.player.BoxPlayerMapImpl;
import net.okocraft.box.storage.api.model.Storage;
import net.okocraft.box.storage.api.util.item.ItemLoader;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoxCore implements BoxAPI {

    private final PluginContext context;
    private final BoxEventManager eventManager;

    private final List<BoxFeature> features = new ArrayList<>();

    private Storage storage;
    private BoxItemManager itemManager;
    private BoxStockManager stockManager;
    private BoxUserManager userManager;
    private BoxCustomDataManager customDataManager;
    private BoxPlayerMapImpl playerMap;

    private BoxCommandImpl boxCommand;
    private BoxAdminCommandImpl boxAdminCommand;

    public BoxCore(@NotNull PluginContext context) {
        this.context = context;
        this.eventManager = new BoxEventManager(context.eventServiceProvider(), context.scheduler());
    }

    public boolean enable(@NotNull Storage storage) {
        if (this.context.config().coreSetting().debug()) {
            DebugListener.register(this.eventManager);
            BoxLogger.logger().info("Debug mode is ENABLED");
        }

        this.storage = storage;
        BoxLogger.logger().info("Initializing {} storage...", storage.getName());

        try {
            storage.init();
            storage.getCustomDataStorage().updateFormatIfNeeded(); // Update data format on database
        } catch (Exception e) {
            BoxLogger.logger().error("Could not initialize {} storage.", storage.getName(), e);
            return false;
        }

        BoxLogger.logger().info("Initializing managers...");

        userManager = new BoxUserManager(storage.getUserStorage());

        try {
            var itemLoadResult = ItemLoader.load(storage.getItemStorage(), this.context.defaultItemProvider());
            itemLoadResult.logItemCount();
            this.itemManager = new BoxItemManager(storage.getItemStorage(), this.eventManager, this.context.scheduler(), itemLoadResult.asIterator());
        } catch (Exception e) {
            BoxLogger.logger().error("Could not load default/custom items", e);
            return false;
        }

        this.stockManager = new BoxStockManager(storage.getStockStorage(), this.eventManager, this.itemManager::getBoxItemOrNull, 300, 15, TimeUnit.SECONDS); // TODO: configurable

        this.customDataManager = new BoxCustomDataManager(storage.getCustomDataStorage());

        this.playerMap = new BoxPlayerMapImpl(this.userManager, this.stockManager, this.eventManager, this.context.scheduler());
        this.playerMap.loadAll();

        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this.playerMap), context.plugin());

        stockManager.schedulerAutoSaveTask(this.context.scheduler());

        BoxLogger.logger().info("Registering commands...");

        this.boxCommand = new BoxCommandImpl(this.context.scheduler(), this.playerMap, this::canUseBox);
        this.boxAdminCommand = new BoxAdminCommandImpl(this.context.scheduler());

        this.context.commandRegisterer().register(this.boxCommand).register(this.boxAdminCommand);

        Bukkit.getPluginManager().registerEvents(this.boxCommand, this.context.plugin());
        Bukkit.getPluginManager().registerEvents(this.boxAdminCommand, this.context.plugin());

        return true;
    }

    public void disable() {
        if (playerMap != null) {
            playerMap.unloadAll();
        }

        stockManager.close();

        DebugListener.unregister(this.eventManager);

        BoxLogger.logger().info("Closing the storage...");

        try {
            storage.close();
        } catch (Exception e) {
            BoxLogger.logger().error("Could not close the storage.", e);
        }

        BoxLogger.logger().info("Unloading languages...");
        // TODO: translationDirectory.unload();
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

        DebugListener.unregister(this.eventManager);

        if (!(sender instanceof ConsoleCommandSender)) {
            BoxLogger.logger().info("Reloading box...");
        }

        try {
            this.context.config().reload();
            sender.sendMessage(MicsMessages.CONFIG_RELOADED);
        } catch (Throwable e) {
            playerMessenger.accept(() -> ErrorMessages.ERROR_RELOAD_FAILURE.apply(Config.FILENAME, e));
            BoxLogger.logger().error("Could not reload {}", Config.FILENAME, e);
        }

        if (this.context.config().coreSetting().debug()) {
            DebugListener.register(this.eventManager);
            BoxLogger.logger().info("Debug mode is ENABLED");
        }

        try {
            // TODO: translationDirectory.load();
            sender.sendMessage(MicsMessages.LANGUAGES_RELOADED);
        } catch (Throwable e) {
            playerMessenger.accept(() -> ErrorMessages.ERROR_RELOAD_FAILURE.apply("languages", e));
            BoxLogger.logger().error("Could not reload languages", e);
        }

        for (var feature : features) {
            if (feature instanceof Reloadable reloadable) {
                try {
                    reloadable.reload(sender);
                    this.eventManager.call(new FeatureEvent(feature, FeatureEvent.Type.RELOAD));
                } catch (Throwable e) {
                    playerMessenger.accept(() -> ErrorMessages.ERROR_RELOAD_FAILURE.apply(feature.getName(), e));
                    BoxLogger.logger().error("Could not reload {}", feature.getName(), e);
                }
            }
        }

        if (!(sender instanceof ConsoleCommandSender)) {
            BoxLogger.logger().info("Successfully reloaded!");
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
    public @NotNull BoxEventManager getEventManager() {
        return this.eventManager;
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
    public @NotNull BoxCustomDataManager getCustomDataManager() {
        return this.customDataManager;
    }

    @Override
    public @NotNull BoxScheduler getScheduler() {
        return this.context.scheduler();
    }

    @Override
    public @NotNull BoxPlayerMap getBoxPlayerMap() {
        return playerMap;
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
        var dependencies = boxFeature.getDependencies();

        if (!dependencies.isEmpty()) {
            for (var dependencyClass : dependencies) {
                if (features.stream().noneMatch(feature -> dependencyClass.isAssignableFrom(feature.getClass()))) {
                    BoxLogger.logger().warn("{} that is the dependency of the {} is not registered.", dependencyClass.getSimpleName(), boxFeature.getName());
                    return;
                }
            }
        }

        try {
            boxFeature.enable();
        } catch (Throwable throwable) {
            BoxLogger.logger().error("Could not enable the {} feature.", boxFeature.getName(), throwable);
            boxFeature.disable();
            return;
        }

        features.add(boxFeature);

        this.eventManager.call(new FeatureEvent(boxFeature, FeatureEvent.Type.REGISTER));

        BoxLogger.logger().info("The {} feature has been enabled.", boxFeature.getName());
    }

    @Override
    public void unregister(@NotNull BoxFeature boxFeature) {
        BoxLogger.logger().info("Disabling the {} feature...", boxFeature.getName());
        features.remove(boxFeature);

        try {
            boxFeature.disable();
        } catch (Throwable throwable) {
            BoxLogger.logger().error("Could not disable the {} feature.", boxFeature.getName(), throwable);
        }

        this.eventManager.call(new FeatureEvent(boxFeature, FeatureEvent.Type.UNREGISTER));
    }

    public void unregisterAllFeatures() {
        if (!this.features.isEmpty()) {
            BoxLogger.logger().info("Disabling features...");

            for (var feature : this.features) {
                BoxLogger.logger().info("Disabling {} feature...", feature.getName());
                features.remove(feature);

                try {
                    feature.disable();
                } catch (Throwable throwable) {
                    BoxLogger.logger().error("Could not disable {} feature.", feature.getName(), throwable);
                }

                this.eventManager.call(new FeatureEvent(feature, FeatureEvent.Type.UNREGISTER));
            }
        }
    }

    @Override
    public boolean canUseBox(@NotNull Player player) {
        return !isDisabledWorld(player.getWorld()) || player.hasPermission("box.admin.ignore-disabled-world");
    }

    @Override
    public boolean isDisabledWorld(@NotNull World world) {
        return isDisabledWorld(world.getName());
    }

    @Override
    public boolean isDisabledWorld(@NotNull String worldName) {
        return this.context.config().coreSetting().disabledWorlds().contains(worldName);
    }

    @Override
    public @NotNull NamespacedKey createNamespacedKey(@NotNull String value) {
        return new NamespacedKey(context.plugin(), value);
    }
}
