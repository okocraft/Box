package net.okocraft.box.plugin;

import net.okocraft.box.plugin.config.GeneralConfig;
import net.okocraft.box.plugin.config.RecipeConfig;
import net.okocraft.box.plugin.config.SoundConfig;
import net.okocraft.box.plugin.database.Storage;
import net.okocraft.box.plugin.listener.AbstractListener;
import net.okocraft.box.plugin.listener.ItemPickupListener;
import net.okocraft.box.plugin.listener.PlayerConnectionListener;
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
import java.util.logging.Level;

public final class Box extends JavaPlugin {

    private GeneralConfig generalConfig;
    private RecipeConfig recipeConfig;
    private SoundConfig soundConfig;

    private Storage storage;

    private ItemManager itemManager;
    private UserManager userManager;
    private SoundPlayer soundPlayer;

    private List<AbstractListener> listeners;

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

        Instant finish = Instant.now();
        getLogger().info("Loading completed! (" + Duration.between(start, finish).toMillis() + "ms)");
    }

    @Override
    public void onEnable() {
        Instant start = Instant.now();

        getLogger().info("Registering event listeners...");

        listeners = new LinkedList<>();

        listeners.add(new PlayerConnectionListener(this));

        if (generalConfig.isAutoStoreEnabled()) {
            listeners.add(new ItemPickupListener(this));
        }

        listeners.forEach(AbstractListener::start);

        getLogger().info("Initializing sound player...");
        soundPlayer = new SoundPlayer(this);

        Instant finish = Instant.now();
        getLogger().info("Box enabled! (" + Duration.between(start, finish).toMillis() + "ms)");
    }

    @Override
    public void onDisable() {
        Instant start = Instant.now();

        getLogger().info("Unregistering event listeners...");
        listeners.forEach(AbstractListener::shutdown);
        listeners.clear();
        listeners = null;

        getLogger().info("Shutting down storage...");
        storage.shutdown();
        storage = null;

        getLogger().info("Unloading config files...");
        unloadConfig();

        getLogger().info("Shutting down sound player...");
        soundPlayer = null;

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
    public ItemManager getItemManager() {
        return itemManager;
    }

    @NotNull
    public UserManager getUserManager() {
        return userManager;
    }

    public SoundPlayer getSoundPlayer() {
        return soundPlayer;
    }

    public void debug(@NotNull String log) {
        // TODO
    }

    @NotNull
    public ExecutorService getExecutor() {
        // TODO
        return null;
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
