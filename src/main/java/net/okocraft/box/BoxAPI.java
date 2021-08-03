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

/**
 * BoxプラグインのAPIクラス。{@code Box.getInstance().getAPI()}からアクセスできる。
 */
@SuppressWarnings("deprecation")
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
        playerData = new PlayerData(config);
        itemData = new ItemData(playerData);
    }

    /**
     * {@code config.yml}の設定内容を取得するクラスを返す。
     * 
     * @return config
     */
    public Config getConfig() {
        return config;
    }

    /**
     * {@code messages.yml}の設定内容を取得・使用できるクラスを返す。
     * 
     * @return messages
     */
    public Messages getMessages() {
        return messages;
    }

    /**
     * {@code layout.yml}の設定内容を取得するクラスを返す。
     * 
     * @return layout
     */
    public Layouts getLayouts() {
        return layout;
    }

    /**
     * {@code prices.yml}の設定内容を取得するクラスを返す。
     * 
     * @return prices
     */
    public Prices getPrices() {
        return prices;
    }

    /**
     * {@code categories.yml}の設定内容を取得するクラスを返す。
     * 
     * @return categories
     */
    public Categories getCategories() {
        return categories;
    }

    /**
     * {@code crafrrecipes.yml}の設定内容を取得するクラスを返す。
     * 
     * @return crafrRecipes
     */
    public CraftRecipes getCraftRecipes() {
        return craftRecipes;
    }

    /**
     * プレイヤーのデータを設定・取得できるクラスを返す。
     * 
     * @return playerData
     */
    public PlayerData getPlayerData() {
        return playerData;
    }

    /**
     * アイテムの内部名、その内部名から実際のアイテムスタックを取得できるクラスを返す。
     * 
     * @return itemData
     */
    public ItemData getItemData() {
        return itemData;
    }

    /**
     * ymlファイルから読み込むたぐいの設定を全てリロードする。
     * データベースに関する設定はリロードできない。
     */
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
        craftRecipes.reload();
    }
}