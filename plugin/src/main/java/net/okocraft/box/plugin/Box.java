package net.okocraft.box.plugin;

import net.okocraft.box.plugin.database.Storage;
import net.okocraft.box.plugin.model.manager.ItemManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Box extends JavaPlugin {

    private ItemManager itemManager;

    @Override
    public void onLoad() {
        // storage initialization

        itemManager = new ItemManager(this);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @NotNull
    public Storage getStorage() {
        return null; // TODO
    }

    @NotNull
    public ItemManager getItemManager() {
        return itemManager;
    }
}
