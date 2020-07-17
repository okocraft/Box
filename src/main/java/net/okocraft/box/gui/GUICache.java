package net.okocraft.box.gui;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

/**
 * いちいち初期化する処理が重いGUIをキャッシュするクラス。リロードのたびにキャッシュは削除される。
 */
public final class GUICache {

    private final static Map<Player, GUICache> cache = new HashMap<>();

    private final Player owner;

    private final Map<String, StrageGUI> categoryStrageGUIMap = new HashMap<>();
    private final Map<String, ShopGUI> categoryShopGUIMap = new HashMap<>();
    private final Map<String, CraftGUI> categoryCraftGUIMap = new HashMap<>();

    private GUICache(Player player) {
        this.owner = player;
    }

    /**
     * プレイヤーのGUIキャッシュを取得する。
     * キャッシュにはプレイヤーが開いたGUIのインスタンスが保存されている。
     * 
     * @param player プレイヤー
     * @return キャッシュ
     */
    public static GUICache getCache(Player player) {
        GUICache result = cache.get(player);
        if (result != null) {
            return result;
        }
        GUICache newCache = new GUICache(player);
        cache.put(player, newCache);
        return newCache;
    }

    /**
     * プレイヤーのキャッシュを削除する。
     * 
     * @param player プレイヤー
     */
    public static void removeCache(Player player) {
        cache.remove(player);
    }

    /**
     * ストレージGUIのキャッシュを取得する。
     * 
     * @param categoryName キャッシュを取得するGUIのカテゴリ
     * @param quantity 取得したGUIに適応するquantity
     * @param page 取得したGUIが開いているべきページ
     * @return GUIのインスタンス
     */
    StrageGUI getStrageGUICache(String categoryName, int quantity, int page) {
        StrageGUI result = categoryStrageGUIMap.get(categoryName);
        if (result == null) {
            result = new StrageGUI(owner, categoryName, quantity);
        }
        result.setQuantity(quantity);
        result.setPage(page);
        categoryStrageGUIMap.put(categoryName, result);
        return result;
    }

    /**
     * ショップGUIのキャッシュを取得する。
     * 
     * @param categoryName キャッシュを取得するGUIのカテゴリ
     * @param quantity 取得したGUIに適応するquantity
     * @param page 取得したGUIが開いているべきページ
     * @return GUIのインスタンス
     */
    ShopGUI getShopGUICache(String categoryName, int quantity, int page) {
        ShopGUI result = categoryShopGUIMap.get(categoryName);
        if (result == null) {
            result = new ShopGUI(owner, categoryName, quantity);
        }
        result.setQuantity(quantity);
        result.setPage(page);
        categoryShopGUIMap.put(categoryName, result);
        return result;
    }

    /**
     * クラフトGUIのキャッシュを取得する。
     * 
     * @param categoryName キャッシュを取得するGUIのカテゴリ
     * @param quantity 取得したGUIに適応するquantity
     * @param page 取得したGUIが開いているべきページ
     * @return GUIのインスタンス
     */
    CraftGUI getCraftGUICache(String categoryName, int quantity, int page) {
        CraftGUI result = categoryCraftGUIMap.get(categoryName);
        if (result == null) {
            result = new CraftGUI(owner, categoryName, quantity);
        }
        result.setQuantity(quantity);
        result.refresh();
        categoryCraftGUIMap.put(categoryName, result);
        return result;
    }
}