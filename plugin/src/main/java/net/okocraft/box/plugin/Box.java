package net.okocraft.box.plugin;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.okocraft.box.plugin.category.CategoryManager;
import net.okocraft.box.plugin.config.GeneralConfig;
import net.okocraft.box.plugin.config.RecipeConfig;
import net.okocraft.box.plugin.config.SoundConfig;
import net.okocraft.box.plugin.database.Storage;
import net.okocraft.box.plugin.listener.AbstractListener;
import net.okocraft.box.plugin.listener.ItemPickupListener;
import net.okocraft.box.plugin.listener.PlayerConnectionListener;
import net.okocraft.box.plugin.listener.stick.BlockPlaceListener;
import net.okocraft.box.plugin.listener.stick.InteractListener;
import net.okocraft.box.plugin.listener.stick.ItemBreakListener;
import net.okocraft.box.plugin.listener.stick.ItemConsumeListener;
import net.okocraft.box.plugin.listener.stick.ProjectileLaunchListener;
import net.okocraft.box.plugin.model.DataHandler;
import net.okocraft.box.plugin.model.manager.ItemManager;
import net.okocraft.box.plugin.model.manager.UserManager;
import net.okocraft.box.plugin.sound.SoundPlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

public final class Box extends JavaPlugin {

    private GeneralConfig generalConfig;
    private RecipeConfig recipeConfig;
    private SoundConfig soundConfig;

    private Storage storage;

    private DataHandler dataHandler;
    private ItemManager itemManager;
    private UserManager userManager;

    private CategoryManager categoryManager;
    private SoundPlayer soundPlayer;

    private List<AbstractListener> listeners;

    private ExecutorService executor;
    private ExecutorService threadPool;
    private ScheduledExecutorService scheduler;

    @Override
    public void onLoad() {
        Instant start = Instant.now();

        getLogger().info("Loading config files...");
        loadConfig();

        try {
            getLogger().info("Starting storage...");
            storage = new Storage(this);
        } catch (Throwable e) {
            getLogger().log(Level.SEVERE, "Failed to connect to database, so disabling plugin...", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        itemManager = new ItemManager(this);
        userManager = new UserManager(this);

        getLogger().info("Loading categories...");
        categoryManager = new CategoryManager(this);
        categoryManager.load();

        Instant finish = Instant.now();
        getLogger().info("Loading completed! (" + Duration.between(start, finish).toMillis() + "ms)");
    }

    @Override
    public void onEnable() {
        Instant start = Instant.now();

        getLogger().info("Initializing executors...");
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Box-Executor"));
        threadPool = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Box-Worker-%d").build()
        );
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Box-Executor"));

        getLogger().info("Registering event listeners...");

        listeners = new LinkedList<>();

        listeners.add(new PlayerConnectionListener(this));

        if (generalConfig.isAutoStoreEnabled()) {
            listeners.add(new ItemPickupListener(this));
        }

        if (generalConfig.isStickEnabled()) {
            listeners.addAll(List.of(
                    new BlockPlaceListener(this),
                    new InteractListener(this),
                    new ItemBreakListener(this),
                    new ItemConsumeListener(this),
                    new ProjectileLaunchListener(this)
            ));
        }

        listeners.forEach(AbstractListener::start);

        getLogger().info("Initializing sound player...");
        soundPlayer = new SoundPlayer(this);

        dataHandler = new DataHandler(this);

        Instant finish = Instant.now();
        getLogger().info("Box enabled! (" + Duration.between(start, finish).toMillis() + "ms)");
    }

    @Override
    public void onDisable() {
        Instant start = Instant.now();

        getLogger().info("Unregistering event listeners...");
        listeners.forEach(AbstractListener::shutdown);
        listeners.clear();

        getLogger().info("Shutting down storage...");
        storage.shutdown();

        getLogger().info("Unloading config files...");
        unloadConfig();

        getLogger().info("Shutting down executors...");
        executor.shutdownNow();
        threadPool.shutdownNow();
        scheduler.shutdownNow();

        Instant finish = Instant.now();
        getLogger().info("Box disabled! (" + Duration.between(start, finish).toMillis() + "ms)");
    }

    @NotNull
    public GeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    @NotNull
    public RecipeConfig getRecipeConfig() {
        return recipeConfig;
    }

    @NotNull
    public SoundConfig getSoundConfig() {
        return soundConfig;
    }

    @NotNull
    public Storage getStorage() {
        return storage;
    }

    @NotNull
    public DataHandler getDataHandler() {
        return dataHandler;
    }

    @NotNull
    public ItemManager getItemManager() {
        return itemManager;
    }

    @NotNull
    public UserManager getUserManager() {
        return userManager;
    }

    @NotNull
    public CategoryManager getCategoryManager() {
        return categoryManager;
    }

    public SoundPlayer getSoundPlayer() {
        return soundPlayer;
    }

    public void debug(@NotNull String log) {
        if (generalConfig.isDebugMode()) {
            getLogger().info(log);
        }
    }

    @NotNull
    public ExecutorService getExecutor() {
        return executor;
    }

    @NotNull
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    @NotNull
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    private void loadConfig() {
        generalConfig = new GeneralConfig(this);

        if (!generalConfig.isLoaded()) {
            printConfigLoadError("config.yml");
        }

        recipeConfig = new RecipeConfig(this);

        if (!recipeConfig.isLoaded()) {
            printConfigLoadError("recipe.yml");
        }

        soundConfig = new SoundConfig(this);

        if (!soundConfig.isLoaded()) {
            printConfigLoadError("sound.yml");
        }
    }

    private void unloadConfig() {
        generalConfig = null;
        recipeConfig = null;
        soundConfig = null;
    }

    private void printConfigLoadError(@NotNull String fileName) {
        getLogger().warning(fileName + " could not be loaded. Continue with default settings...");
    }
}
