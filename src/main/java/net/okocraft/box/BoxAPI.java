package net.okocraft.box;

import org.bukkit.Bukkit;

import net.okocraft.box.config.Categories;
import net.okocraft.box.config.Config;
import net.okocraft.box.config.CraftRecipes;
import net.okocraft.box.config.Layouts;
import net.okocraft.box.config.Messages;
import net.okocraft.box.config.Prices;
import net.okocraft.box.database.ItemData;
import net.okocraft.box.database.PlayerData;
import net.okocraft.box.gui.GUICache;

public final class BoxAPI {

    private final Config config = new Config();
    private final Messages messages = new Messages();
    private final Layouts layout = new Layouts();
    private final Prices prices = new Prices();
    private final Categories categories = new Categories();
    private final CraftRecipes craftRecipes = new CraftRecipes();

    private final PlayerData playerData;
    private final ItemData itemData;


    BoxAPI() {
        reloadAllConfigs();
        playerData = new PlayerData(Box.getInstance().getDataFolder().toPath().resolve("database.db"));
        itemData = new ItemData(playerData);
    }

    public Config getConfig() {
        return config;
    }

    public Messages getMessages() {
        return messages;
    }

    public Layouts getLayouts() {
        return layout;
    }

    public Prices getPrices() {
        return prices;
    }

    public Categories getCategories() {
        return categories;
    }

    public CraftRecipes getCraftRecipes() {
        return craftRecipes;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public ItemData getItemData() {
        return itemData;
    }

    public void reloadAllConfigs() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.closeInventory();            
            GUICache.removeCache(player);
        });

        config.reload();
        messages.reload();
        layout.reload();
        prices.reload();
        categories.reload();
    }
}